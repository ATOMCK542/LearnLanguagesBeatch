package dev.sergey.triad.data.db

import dev.sergey.triad.data.content.LocalizedDto
import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.Concept
import dev.sergey.triad.domain.ConceptKind
import dev.sergey.triad.domain.ConceptText
import dev.sergey.triad.domain.FsrsCardState
import dev.sergey.triad.domain.LocalizedText
import dev.sergey.triad.domain.PathProgress
import dev.sergey.triad.domain.PrimerProgress
import dev.sergey.triad.domain.Profile
import dev.sergey.triad.domain.ReviewItem
import dev.sergey.triad.domain.Theme
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object Mappers {
    private val json = Json { ignoreUnknownKeys = true }

    fun ProfileEntity.toDomain() = Profile(
        id = id,
        displayName = displayName,
        email = email,
        createdAt = createdAt,
        lastOpenedAt = lastOpenedAt,
        uiLang = AppLanguage.fromCode(uiLang),
        nativeLang = AppLanguage.fromCode(nativeLang),
        targetLangs = targetLangs.split(",").filter { it.isNotBlank() }.map { AppLanguage.fromCode(it) },
        newLimit = newLimit,
        ttsEnabled = ttsEnabled,
        streakDays = streakDays,
        lastStudyDay = lastStudyDay,
        reviewsDone = reviewsDone,
    )

    fun Profile.toEntity() = ProfileEntity(
        id = if (id == 0L) 0 else id,
        displayName = displayName,
        email = email,
        createdAt = createdAt,
        lastOpenedAt = lastOpenedAt,
        uiLang = uiLang.code,
        nativeLang = nativeLang.code,
        targetLangs = targetLangs.joinToString(",") { it.code },
        newLimit = newLimit,
        ttsEnabled = ttsEnabled,
        streakDays = streakDays,
        lastStudyDay = lastStudyDay,
        reviewsDone = reviewsDone,
    )

    fun Theme.toEntity() = ThemeEntity(
        id = id,
        kind = kind,
        sortOrder = sortOrder,
        titleJson = json.encodeToString(LocalizedDto(title.en, title.ru, title.vi)),
        descriptionJson = json.encodeToString(LocalizedDto(description.en, description.ru, description.vi)),
    )

    fun ThemeEntity.toDomain(): Theme {
        val title = json.decodeFromString(LocalizedDto.serializer(), titleJson)
        val description = json.decodeFromString(LocalizedDto.serializer(), descriptionJson)
        return Theme(id, kind, sortOrder, title.toDomain(), description.toDomain())
    }

    fun Concept.toEntity() = ConceptEntity(
        id = id,
        kind = kind.name.lowercase(),
        themeId = themeId,
        tags = tags.joinToString(","),
        grammarJson = json.encodeToString(LocalizedDto(grammar.en, grammar.ru, grammar.vi)),
    )

    fun ConceptText.toEntity(conceptId: String) = ConceptTextEntity(
        conceptId = conceptId,
        lang = lang.code,
        text = text,
        ipa = ipa,
        hintsJson = json.encodeToString(hints.mapKeys { it.key.code }),
        tones = tones.joinToString(","),
    )

    fun assembleConcept(
        entity: ConceptEntity,
        texts: List<ConceptTextEntity>,
    ): Concept {
        val grammar = json.decodeFromString(LocalizedDto.serializer(), entity.grammarJson)
        return Concept(
            id = entity.id,
            kind = when (entity.kind) {
                "phrase" -> ConceptKind.Phrase
                "sentence" -> ConceptKind.Sentence
                else -> ConceptKind.Word
            },
            themeId = entity.themeId,
            tags = entity.tags.split(",").filter { it.isNotBlank() },
            grammar = grammar.toDomain(),
            texts = texts.associate { row ->
                val lang = AppLanguage.fromCode(row.lang)
                val hints = json.decodeFromString<Map<String, String>>(row.hintsJson)
                    .mapKeys { AppLanguage.fromCode(it.key) }
                lang to ConceptText(
                    lang = lang,
                    text = row.text,
                    ipa = row.ipa,
                    hints = hints,
                    tones = row.tones.split(",").filter { it.isNotBlank() },
                )
            },
        )
    }

    fun ReviewItem.toEntity() = ReviewItemEntity(
        profileId, conceptId, targetLang.code, stability, difficulty, dueAt, lastReviewAt,
        reps, lapses, state.name, elapsedDays, scheduledDays,
    )

    fun ReviewItemEntity.toDomain() = ReviewItem(
        profileId, conceptId, AppLanguage.fromCode(targetLang), stability, difficulty, dueAt,
        lastReviewAt, reps, lapses, FsrsCardState.valueOf(state), elapsedDays, scheduledDays,
    )

    fun PrimerProgressEntity.toDomain() = PrimerProgress(profileId, primerId, completed)
    fun PathProgressEntity.toDomain() = PathProgress(profileId, themeId, unlocked, completedCount)
}
