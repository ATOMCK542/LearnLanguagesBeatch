package dev.sergey.triad.data.repo

import dev.sergey.triad.data.ActiveProfileStore
import dev.sergey.triad.data.backup.BackupCodec
import dev.sergey.triad.data.backup.BackupPayload
import dev.sergey.triad.data.content.PackImporter
import dev.sergey.triad.data.db.Mappers.toDomain
import dev.sergey.triad.data.db.Mappers.toEntity
import dev.sergey.triad.data.db.Mappers.assembleConcept
import dev.sergey.triad.data.db.MasteredConceptEntity
import dev.sergey.triad.data.db.PathProgressEntity
import dev.sergey.triad.data.db.PrimerProgressEntity
import dev.sergey.triad.data.db.ReviewItemEntity
import dev.sergey.triad.data.db.TriadDatabase
import dev.sergey.triad.data.db.UserCardEntity
import dev.sergey.triad.data.scheduler.FsrsScheduler
import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.Concept
import dev.sergey.triad.domain.ConceptKind
import dev.sergey.triad.domain.ConceptText
import dev.sergey.triad.domain.ExerciseFactory
import dev.sergey.triad.domain.LessonProgress
import dev.sergey.triad.domain.LocalizedText
import dev.sergey.triad.domain.PracticePlanner
import dev.sergey.triad.domain.ThemeStudy
import dev.sergey.triad.domain.Profile
import dev.sergey.triad.domain.Rating
import dev.sergey.triad.domain.ReviewItem
import dev.sergey.triad.domain.SessionItem
import dev.sergey.triad.domain.SessionPlan
import dev.sergey.triad.domain.SessionPlanner
import dev.sergey.triad.domain.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TriadRepository @Inject constructor(
    private val db: TriadDatabase,
    private val activeStore: ActiveProfileStore,
    private val importer: PackImporter,
    val scheduler: FsrsScheduler,
) {
    val profiles: Flow<List<Profile>> = db.profiles().observeAll().map { list -> list.map { it.toDomain() } }
    val activeProfileId: Flow<Long?> = activeStore.activeProfileId

    suspend fun bootstrap() {
        importer.importIfNeeded()
    }

    suspend fun createProfile(
        displayName: String,
        email: String?,
        uiLang: AppLanguage,
        nativeLang: AppLanguage,
        targetLangs: List<AppLanguage>,
    ): Profile {
        val now = System.currentTimeMillis()
        val entity = Profile(
            id = 0,
            displayName = displayName.ifBlank { "Learner" },
            email = email?.ifBlank { null },
            createdAt = now,
            lastOpenedAt = now,
            uiLang = uiLang,
            nativeLang = nativeLang,
            targetLangs = targetLangs.ifEmpty { AppLanguage.all.filter { it != nativeLang } },
            newLimit = 20,
            ttsEnabled = true,
            streakDays = 0,
            lastStudyDay = null,
            reviewsDone = 0,
        ).toEntity()
        val id = db.profiles().upsert(entity)
        val created = db.profiles().byId(id)?.toDomain() ?: entity.copy(id = id).toDomain()
        unlockFirstThemes(created)
        activeStore.set(created.id)
        return created
    }

    suspend fun switchProfile(id: Long) {
        val profile = db.profiles().byId(id)?.toDomain() ?: return
        db.profiles().upsert(profile.copy(lastOpenedAt = System.currentTimeMillis()).toEntity())
        activeStore.set(id)
    }

    suspend fun deleteProfile(id: Long) {
        db.deleteProfileCascade(id)
        val remaining = db.profiles().all()
        activeStore.set(remaining.firstOrNull()?.id)
    }

    suspend fun updateProfile(profile: Profile) {
        db.profiles().upsert(profile.toEntity())
        unlockFirstThemes(profile)
        ensureContentThemeUnlocked(profile)
    }

    suspend fun activeProfile(): Profile? {
        val id = activeStore.get() ?: db.profiles().all().firstOrNull()?.id ?: return null
        return db.profiles().byId(id)?.toDomain()
    }

    suspend fun themes(): List<Theme> = db.catalog().themes().map { it.toDomain() }

    suspend fun concepts(): List<Concept> {
        val texts = db.catalog().texts().groupBy { it.conceptId }
        return db.catalog().concepts().map { assembleConcept(it, texts[it.id].orEmpty()) }
    }

    suspend fun dueCount(now: Long = System.currentTimeMillis()): Int {
        val profile = activeProfile() ?: return 0
        return db.progress().dueCount(profile.id, now)
    }

    suspend fun planSession(
        now: Long = System.currentTimeMillis(),
        preferredThemeId: String? = null,
        sessionSize: Int = 10,
    ): SessionPlan {
        val profile = activeProfile() ?: return SessionPlan(emptyList())
        ensureContentThemeUnlocked(profile)
        val allConcepts = concepts()
        val unlocked = unlockedThemeIds(profile)
        val visible = allConcepts.filter { it.themeId in unlocked }
        val reviews = db.progress().reviews(profile.id).map { it.toDomain() }
        val factory = ExerciseFactory { lang ->
            visible.map { it.text(lang).text }
        }
        return SessionPlanner(scheduler, factory).plan(
            profile,
            visible,
            reviews,
            now,
            sessionSize = sessionSize,
            preferredThemeId = preferredThemeId,
        )
    }

    suspend fun planPractice(limit: Int, now: Long = System.currentTimeMillis()): SessionPlan {
        val profile = activeProfile() ?: return SessionPlan(emptyList(), practice = true)
        val pool = practicePool(profile)
        val picked = PracticePlanner.pick(pool, limit, kotlin.random.Random.Default)
        val allConcepts = concepts()
        val unlocked = unlockedThemeIds(profile)
        val visible = allConcepts.filter { it.themeId in unlocked }
        val factory = ExerciseFactory { lang -> visible.map { it.text(lang).text } }
        val conceptMap = allConcepts.associateBy { it.id }
        val targets = profile.studyTargets()
        val siblings = db.progress().reviews(profile.id).map { it.toDomain() }.groupBy { it.conceptId }
        val items = picked.mapIndexedNotNull { index, review ->
            val concept = conceptMap[review.conceptId] ?: return@mapIndexedNotNull null
            val cardReviews = siblings[review.conceptId]
                ?.filter { it.targetLang in targets }
                ?.ifEmpty { listOf(review) }
                ?: listOf(review)
            SessionItem(
                reviews = cardReviews,
                exercise = factory.forReview(
                    concept,
                    cardReviews.first(),
                    profile.nativeLang,
                    now,
                    index,
                    targets,
                ),
            )
        }
        return SessionPlan(items, practice = true)
    }

    suspend fun practicePoolSize(): Int {
        val profile = activeProfile() ?: return 0
        return practicePool(profile).size
    }

    suspend fun masteredConceptIds(profileId: Long): Set<String> =
        db.progress().mastered(profileId).map { it.conceptId }.toSet()

    suspend fun setMastered(conceptId: String, mastered: Boolean) {
        val profile = activeProfile() ?: return
        if (mastered) {
            db.progress().upsertMastered(MasteredConceptEntity(profile.id, conceptId))
        } else {
            db.progress().deleteMastered(profile.id, conceptId)
        }
    }

    fun preview(item: ReviewItem, now: Long = System.currentTimeMillis()): Map<Rating, ReviewItem> =
        scheduler.preview(item, now)

    suspend fun applyRating(item: ReviewItem, rating: Rating, now: Long = System.currentTimeMillis()) {
        val updated = scheduler.review(item, rating, now)
        db.progress().upsertReview(updated.toEntity())
        val profile = db.profiles().byId(item.profileId)?.toDomain() ?: return
        val today = LocalDate.now(ZoneId.systemDefault()).toString()
        val streak = if (profile.lastStudyDay == today) {
            profile.streakDays
        } else if (profile.lastStudyDay == LocalDate.now().minusDays(1).toString()) {
            profile.streakDays + 1
        } else {
            1
        }
        db.profiles().upsert(
            profile.copy(
                streakDays = streak,
                lastStudyDay = today,
                reviewsDone = profile.reviewsDone + 1,
                lastOpenedAt = now,
            ).toEntity(),
        )
        bumpPath(profile.id, updated.conceptId)
    }

    suspend fun gradeLesson(
        reviews: List<ReviewItem>,
        results: Map<AppLanguage, Boolean>,
        now: Long = System.currentTimeMillis(),
    ) {
        reviews.distinctBy { it.conceptId to it.targetLang }.forEach { review ->
            val ok = results[review.targetLang]
            if (ok == null && review.reps > 0) return@forEach
            applyRating(review, if (ok == false) Rating.Again else Rating.Good, now)
        }
    }

    suspend fun enrollSession(items: List<SessionItem>, now: Long = System.currentTimeMillis()) {
        val profile = activeProfile() ?: return
        val existing = db.progress().reviews(profile.id).map { it.toDomain() }
        LessonProgress.stillUnseen(items.flatMap { it.reviews }, existing)
            .filter { it.profileId == profile.id }
            .forEach { applyRating(it, Rating.Good, now) }
    }

    suspend fun themeStudy(): Map<String, ThemeStudy> {
        val profile = activeProfile() ?: return emptyMap()
        val primers = themes().filter { it.kind == "primer" }.map { it.id }.toSet()
        return LessonProgress.byTheme(
            profileId = profile.id,
            concepts = concepts(),
            reviews = db.progress().reviews(profile.id).map { it.toDomain() },
            skipThemeIds = primers,
        )
    }

    suspend fun completePrimer(primerId: String) {
        val profile = activeProfile() ?: return
        db.progress().upsertPrimer(PrimerProgressEntity(profile.id, primerId, true))
        unlockNext(profile)
    }

    suspend fun primerDone(primerId: String): Boolean {
        val profile = activeProfile() ?: return false
        return db.progress().primers(profile.id).any { it.primerId == primerId && it.completed }
    }

    suspend fun addUserConcept(
        kind: ConceptKind,
        texts: Map<AppLanguage, String>,
        grammar: LocalizedText,
    ): Concept {
        val profile = activeProfile() ?: error("no profile")
        val id = "user.${profile.id}.${System.currentTimeMillis()}"
        val concept = Concept(
            id = id,
            kind = kind,
            themeId = "user",
            tags = listOf("user"),
            grammar = grammar,
            texts = texts.map { (lang, text) ->
                lang to ConceptText(lang, text, "", emptyMap())
            }.toMap(),
        )
        db.catalog().upsertConcepts(listOf(concept.toEntity()))
        db.catalog().upsertTexts(concept.texts.values.map { it.toEntity(concept.id) })
        db.catalog().upsertUserCard(UserCardEntity(profileId = profile.id, conceptId = id))
        return concept
    }

    suspend fun exportJson(allProfiles: Boolean): String {
        val profiles = db.profiles().all()
        val only = if (allProfiles) null else activeStore.get()
        val reviews = db.progress().allReviews()
        val cards = profiles.flatMap { db.catalog().userCards(it.id) }
        val primers = profiles.flatMap { db.progress().primers(it.id) }
        val path = profiles.flatMap { db.progress().path(it.id) }
        val mastered = profiles.flatMap { db.progress().mastered(it.id) }
        return BackupCodec.encode(
            BackupCodec.fromEntities(profiles, reviews, cards, primers, path, mastered, only),
        )
    }

    suspend fun importJson(raw: String) {
        val decoded = BackupCodec.decode(raw)
        val maxId = db.profiles().all().maxOfOrNull { it.id } ?: 0L
        val remapped = BackupCodec.remapIds(decoded, maxId + 1)
        remapped.profiles.forEach { p ->
            db.profiles().upsert(
                Profile(
                    id = p.id,
                    displayName = p.displayName,
                    email = p.email,
                    createdAt = p.createdAt,
                    lastOpenedAt = p.lastOpenedAt,
                    uiLang = AppLanguage.fromCode(p.uiLang),
                    nativeLang = AppLanguage.fromCode(p.nativeLang),
                    targetLangs = p.targetLangs.split(",").filter { it.isNotBlank() }.map { AppLanguage.fromCode(it) },
                    newLimit = p.newLimit,
                    ttsEnabled = p.ttsEnabled,
                    streakDays = p.streakDays,
                    lastStudyDay = p.lastStudyDay,
                    reviewsDone = p.reviewsDone,
                ).toEntity(),
            )
        }
        db.progress().upsertReviews(
            remapped.reviews.map {
                ReviewItemEntity(
                    it.profileId, it.conceptId, it.targetLang, it.stability, it.difficulty,
                    it.dueAt, it.lastReviewAt, it.reps, it.lapses, it.state, it.elapsedDays, it.scheduledDays,
                )
            },
        )
        remapped.userCards.forEach {
            db.catalog().upsertUserCard(UserCardEntity(profileId = it.profileId, conceptId = it.conceptId))
        }
        remapped.primers.forEach {
            db.progress().upsertPrimer(PrimerProgressEntity(it.profileId, it.primerId, it.completed))
        }
        remapped.path.forEach {
            db.progress().upsertPath(PathProgressEntity(it.profileId, it.themeId, it.unlocked, it.completedCount))
        }
        remapped.mastered.forEach {
            db.progress().upsertMastered(MasteredConceptEntity(it.profileId, it.conceptId))
        }
    }

    suspend fun pathState(profile: Profile): Map<String, PathProgressEntity> {
        ensureContentThemeUnlocked(profile)
        return db.progress().path(profile.id).associateBy { it.themeId }
    }

    suspend fun unlockTheme(themeId: String) {
        val profile = activeProfile() ?: return
        if (themeId.isBlank()) return
        val current = db.progress().path(profile.id).firstOrNull { it.themeId == themeId }
        db.progress().upsertPath(
            current?.copy(unlocked = true) ?: PathProgressEntity(profile.id, themeId, true, 0),
        )
    }

    private suspend fun bumpPath(profileId: Long, conceptId: String) {
        val concept = concepts().firstOrNull { it.id == conceptId } ?: return
        val current = db.progress().path(profileId).firstOrNull { it.themeId == concept.themeId }
            ?: PathProgressEntity(profileId, concept.themeId, true, 0)
        val updated = current.copy(completedCount = current.completedCount + 1, unlocked = true)
        db.progress().upsertPath(updated)
        unlockNext(db.profiles().byId(profileId)!!.toDomain())
    }

    private suspend fun unlockFirstThemes(profile: Profile) {
        val existing = db.progress().path(profile.id).map { it.themeId }.toSet()
        contentUnits().firstOrNull()?.let { theme ->
            if (theme.id !in existing) {
                db.progress().upsertPath(PathProgressEntity(profile.id, theme.id, true, 0))
            }
        }
        themes().filter { it.kind == "primer" }.forEach { theme ->
            val needed = when (theme.id) {
                "primer_vi" -> AppLanguage.Vi in profile.studyTargets()
                "primer_ru" -> AppLanguage.Ru in profile.studyTargets()
                "primer_en" -> AppLanguage.En in profile.studyTargets()
                else -> true
            }
            if (needed && theme.id !in existing) {
                db.progress().upsertPath(PathProgressEntity(profile.id, theme.id, true, 0))
            }
        }
    }

    private suspend fun unlockNext(profile: Profile) {
        val units = contentUnits()
        val path = db.progress().path(profile.id).associateBy { it.themeId }
        units.forEachIndexed { index, theme ->
            val prev = units.getOrNull(index - 1) ?: return@forEachIndexed
            val prevState = path[prev.id]
            if (prevState != null && prevState.completedCount >= 8) {
                db.progress().upsertPath(
                    path[theme.id]?.copy(unlocked = true)
                        ?: PathProgressEntity(profile.id, theme.id, true, 0),
                )
            }
        }
    }

    private suspend fun ensureContentThemeUnlocked(profile: Profile) {
        val units = contentUnits()
        if (units.isEmpty()) return
        val path = db.progress().path(profile.id)
        val unlockedUnits = path.filter { row -> row.unlocked && units.any { it.id == row.themeId } }
        if (unlockedUnits.isEmpty()) {
            units.first().let {
                db.progress().upsertPath(PathProgressEntity(profile.id, it.id, true, 0))
            }
        }
    }

    private suspend fun contentUnits(): List<Theme> =
        themes().filter { it.kind != "primer" }.sortedBy { it.sortOrder }

    private suspend fun unlockedThemeIds(profile: Profile): Set<String> {
        ensureContentThemeUnlocked(profile)
        val path = db.progress().path(profile.id)
        val unlocked = path.filter { it.unlocked }.map { it.themeId }.toMutableSet()
        if (unlocked.none { id -> contentUnits().any { it.id == id } }) {
            contentUnits().firstOrNull()?.id?.let { unlocked += it }
        }
        unlocked += "user"
        return unlocked
    }

    private suspend fun practicePool(profile: Profile): List<ReviewItem> {
        val allConcepts = concepts()
        val unlocked = unlockedThemeIds(profile)
        val primers = themes().filter { it.kind == "primer" }.map { it.id }.toSet()
        val mastered = masteredConceptIds(profile.id)
        val reviews = db.progress().reviews(profile.id).map { it.toDomain() }
        val targets = profile.studyTargets()
        return PracticePlanner.eligible(
            profileId = profile.id,
            targetLangs = targets,
            reviews = reviews,
            concepts = allConcepts,
            unlockedThemeIds = unlocked,
            primerThemeIds = primers,
            masteredConceptIds = mastered,
        )
    }
}
