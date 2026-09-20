package dev.sergey.triad.data.translate

import dev.sergey.triad.domain.AppLanguage

data class TranslationCapabilities(
    val offline: Boolean,
    val realtime: Boolean,
    val languages: Set<AppLanguage>,
)

interface TranslationEngine {
    val capabilities: TranslationCapabilities
    suspend fun isReady(): Boolean
    suspend fun downloadIfNeeded()
    suspend fun translate(text: String, from: AppLanguage, to: AppLanguage): Result<String>
}

class StubTranslationEngine : TranslationEngine {
    override val capabilities = TranslationCapabilities(
        offline = false,
        realtime = false,
        languages = AppLanguage.entries.toSet(),
    )

    override suspend fun isReady(): Boolean = false

    override suspend fun downloadIfNeeded() = Unit

    override suspend fun translate(
        text: String,
        from: AppLanguage,
        to: AppLanguage,
    ): Result<String> = Result.failure(
        UnsupportedOperationException("translator_stub"),
    )
}
