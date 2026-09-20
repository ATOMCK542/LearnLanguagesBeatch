package dev.sergey.triad.data.backup

import dev.sergey.triad.data.db.PathProgressEntity
import dev.sergey.triad.data.db.PrimerProgressEntity
import dev.sergey.triad.data.db.ProfileEntity
import dev.sergey.triad.data.db.ReviewItemEntity
import dev.sergey.triad.data.db.UserCardEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BackupPayload(
    val version: Int = 1,
    val profiles: List<ProfileEntityDto>,
    val reviews: List<ReviewDto>,
    val userCards: List<UserCardDto>,
    val primers: List<PrimerDto>,
    val path: List<PathDto>,
)

@Serializable
data class ProfileEntityDto(
    val id: Long,
    val displayName: String,
    val email: String?,
    val createdAt: Long,
    val lastOpenedAt: Long,
    val uiLang: String,
    val nativeLang: String,
    val targetLangs: String,
    val newLimit: Int,
    val ttsEnabled: Boolean,
    val streakDays: Int,
    val lastStudyDay: String?,
    val reviewsDone: Int,
)

@Serializable
data class ReviewDto(
    val profileId: Long,
    val conceptId: String,
    val targetLang: String,
    val stability: Double,
    val difficulty: Double,
    val dueAt: Long,
    val lastReviewAt: Long?,
    val reps: Int,
    val lapses: Int,
    val state: String,
    val elapsedDays: Double,
    val scheduledDays: Double,
)

@Serializable
data class UserCardDto(val id: Long, val profileId: Long, val conceptId: String)

@Serializable
data class PrimerDto(val profileId: Long, val primerId: String, val completed: Boolean)

@Serializable
data class PathDto(val profileId: Long, val themeId: String, val unlocked: Boolean, val completedCount: Int)

object BackupCodec {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun encode(payload: BackupPayload): String = json.encodeToString(payload)

    fun decode(raw: String): BackupPayload = json.decodeFromString(BackupPayload.serializer(), raw)

    fun fromEntities(
        profiles: List<ProfileEntity>,
        reviews: List<ReviewItemEntity>,
        userCards: List<UserCardEntity>,
        primers: List<PrimerProgressEntity>,
        path: List<PathProgressEntity>,
        onlyProfileId: Long? = null,
    ): BackupPayload {
        val ps = if (onlyProfileId == null) profiles else profiles.filter { it.id == onlyProfileId }
        val ids = ps.map { it.id }.toSet()
        return BackupPayload(
            profiles = ps.map {
                ProfileEntityDto(
                    it.id, it.displayName, it.email, it.createdAt, it.lastOpenedAt,
                    it.uiLang, it.nativeLang, it.targetLangs, it.newLimit, it.ttsEnabled,
                    it.streakDays, it.lastStudyDay, it.reviewsDone,
                )
            },
            reviews = reviews.filter { it.profileId in ids }.map {
                ReviewDto(
                    it.profileId, it.conceptId, it.targetLang, it.stability, it.difficulty,
                    it.dueAt, it.lastReviewAt, it.reps, it.lapses, it.state, it.elapsedDays, it.scheduledDays,
                )
            },
            userCards = userCards.filter { it.profileId in ids }.map { UserCardDto(it.id, it.profileId, it.conceptId) },
            primers = primers.filter { it.profileId in ids }.map { PrimerDto(it.profileId, it.primerId, it.completed) },
            path = path.filter { it.profileId in ids }.map { PathDto(it.profileId, it.themeId, it.unlocked, it.completedCount) },
        )
    }

    fun remapIds(payload: BackupPayload, nextIdStart: Long): BackupPayload {
        var next = nextIdStart
        val map = mutableMapOf<Long, Long>()
        val profiles = payload.profiles.map { p ->
            val newId = next++
            map[p.id] = newId
            p.copy(id = newId)
        }
        fun pid(old: Long) = map[old] ?: old
        return payload.copy(
            profiles = profiles,
            reviews = payload.reviews.map { it.copy(profileId = pid(it.profileId)) },
            userCards = payload.userCards.map { it.copy(id = 0, profileId = pid(it.profileId)) },
            primers = payload.primers.map { it.copy(profileId = pid(it.profileId)) },
            path = payload.path.map { it.copy(profileId = pid(it.profileId)) },
        )
    }
}
