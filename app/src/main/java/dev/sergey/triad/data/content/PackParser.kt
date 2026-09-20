package dev.sergey.triad.data.content

import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.Concept
import dev.sergey.triad.domain.ConceptKind
import dev.sergey.triad.domain.ConceptText
import dev.sergey.triad.domain.LocalizedText
import dev.sergey.triad.domain.Theme
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LocalizedDto(val en: String, val ru: String, val vi: String) {
    fun toDomain() = LocalizedText(en, ru, vi)
}

@Serializable
data class ConceptTextDto(
    val text: String,
    val ipa: String = "",
    val hints: Map<String, String> = emptyMap(),
    val tones: List<String> = emptyList(),
)

@Serializable
data class ConceptDto(
    val id: String,
    val kind: String,
    val theme: String,
    val tags: List<String> = emptyList(),
    val grammar: LocalizedDto,
    val texts: Map<String, ConceptTextDto>,
)

@Serializable
data class ThemeDto(
    val id: String,
    val kind: String = "unit",
    val order: Int,
    val title: LocalizedDto,
    val description: LocalizedDto = LocalizedDto("", "", ""),
)

@Serializable
data class PackDto(
    val theme: ThemeDto? = null,
    val themes: List<ThemeDto> = emptyList(),
    val concepts: List<ConceptDto> = emptyList(),
)

object PackParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(raw: String): PackDto = json.decodeFromString(PackDto.serializer(), raw)

    fun themesOf(pack: PackDto): List<Theme> {
        val dtos = buildList {
            pack.theme?.let { add(it) }
            addAll(pack.themes)
        }
        return dtos.map {
            Theme(it.id, it.kind, it.order, it.title.toDomain(), it.description.toDomain())
        }
    }

    fun conceptsOf(pack: PackDto): List<Concept> = pack.concepts.map { dto ->
        require(dto.texts.keys.containsAll(setOf("en", "ru", "vi"))) {
            "Concept ${dto.id} must have en, ru and vi"
        }
        val texts = dto.texts.map { (code, text) ->
            val lang = AppLanguage.fromCode(code)
            val hints = text.hints
                .filterKeys { it != code }
                .mapKeys { AppLanguage.fromCode(it.key) }
            lang to ConceptText(lang, text.text, text.ipa, hints, text.tones)
        }.toMap()
        Concept(
            id = dto.id,
            kind = when (dto.kind.lowercase()) {
                "phrase" -> ConceptKind.Phrase
                "sentence" -> ConceptKind.Sentence
                else -> ConceptKind.Word
            },
            themeId = dto.theme,
            tags = dto.tags,
            grammar = dto.grammar.toDomain(),
            texts = texts,
        )
    }
}
