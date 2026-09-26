package dev.sergey.triad.data.content

import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.Concept
import dev.sergey.triad.domain.ConceptKind
import dev.sergey.triad.domain.ConceptText
import dev.sergey.triad.domain.LessonBlock
import dev.sergey.triad.domain.LessonSection
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
    val level: Int = 0,
    val uses: List<String> = emptyList(),
)

@Serializable
data class LessonBlockDto(
    val type: String,
    val text: LocalizedDto? = null,
    val lang: String? = null,
    val say: String? = null,
    val caption: LocalizedDto? = null,
) {
    fun toDomain(): LessonBlock = when (type) {
        "text" -> LessonBlock.Paragraph(text?.toDomain() ?: error("text block is missing text"))
        "speak" -> {
            require(lang in SPEAK_LANGS) { "speak lang must be en, ru, or vi" }
            require(!say.isNullOrBlank()) { "speak say is empty" }
            LessonBlock.Speak(
                lang = AppLanguage.fromCode(lang!!),
                say = say,
                caption = caption?.toDomain() ?: error("speak block is missing caption"),
            )
        }
        else -> error("Unknown lesson block: $type")
    }

    companion object {
        private val SPEAK_LANGS = setOf("en", "ru", "vi")

        fun fromDomain(block: LessonBlock): LessonBlockDto = when (block) {
            is LessonBlock.Paragraph -> LessonBlockDto(
                type = "text",
                text = LocalizedDto(block.text.en, block.text.ru, block.text.vi),
            )
            is LessonBlock.Speak -> LessonBlockDto(
                type = "speak",
                lang = block.lang.code,
                say = block.say,
                caption = LocalizedDto(block.caption.en, block.caption.ru, block.caption.vi),
            )
        }
    }
}

@Serializable
data class LessonSectionDto(
    val id: String,
    val title: LocalizedDto,
    val blocks: List<LessonBlockDto> = emptyList(),
) {
    fun toDomain() = LessonSection(
        id = id,
        title = title.toDomain(),
        blocks = blocks.map { it.toDomain() },
    )

    companion object {
        fun fromDomain(section: LessonSection) = LessonSectionDto(
            id = section.id,
            title = LocalizedDto(section.title.en, section.title.ru, section.title.vi),
            blocks = section.blocks.map { LessonBlockDto.fromDomain(it) },
        )
    }
}

@Serializable
data class ThemeBodyDto(
    val description: LocalizedDto,
    val sections: List<LessonSectionDto> = emptyList(),
)

@Serializable
data class ThemeDto(
    val id: String,
    val kind: String = "unit",
    val order: Int,
    val title: LocalizedDto,
    val description: LocalizedDto = LocalizedDto("", "", ""),
    val sections: List<LessonSectionDto> = emptyList(),
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
            Theme(
                it.id,
                it.kind,
                it.order,
                it.title.toDomain(),
                it.description.toDomain(),
                it.sections.map { section -> section.toDomain() },
            )
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
        val uses = dto.uses.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (uses.isNotEmpty()) {
            require(dto.level in 1..3) { "Concept ${dto.id} level must be 1, 2, or 3" }
            require(dto.id !in uses) { "Concept ${dto.id} cannot use itself" }
        }
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
            level = if (uses.isEmpty()) 0 else dto.level,
            uses = uses,
        )
    }
}
