package dev.sergey.triad.unlock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sergey.triad.data.audio.UiClickPlayer
import dev.sergey.triad.data.repo.TriadRepository
import dev.sergey.triad.data.unlock.UnlockGateStore
import dev.sergey.triad.data.unlock.UnlockSession
import dev.sergey.triad.domain.AnswerEvaluator
import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.Concept
import dev.sergey.triad.domain.ExerciseFactory
import dev.sergey.triad.domain.ReviewItem
import dev.sergey.triad.domain.UnlockGate
import dev.sergey.triad.domain.UnlockProgress
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UnlockUiState(
    val ready: Boolean = false,
    val empty: Boolean = false,
    val prompt: String = "",
    val promptLang: AppLanguage = AppLanguage.En,
    val answerLang: AppLanguage = AppLanguage.En,
    val options: List<String> = emptyList(),
    val correct: String = "",
    val picked: String? = null,
    val revealed: Boolean = false,
    val lastCorrect: Boolean? = null,
    val score: Int = 0,
    val required: Int = UnlockGate.DEFAULT_CORRECT,
    val passed: Boolean = false,
)

@HiltViewModel
class UnlockViewModel @Inject constructor(
    private val repo: TriadRepository,
    private val store: UnlockGateStore,
    private val session: UnlockSession,
    private val clicks: UiClickPlayer,
) : ViewModel() {
    private val _state = MutableStateFlow(UnlockUiState())
    val state: StateFlow<UnlockUiState> = _state.asStateFlow()

    private var pool: List<ReviewItem> = emptyList()
    private var concepts: List<Concept> = emptyList()
    private var byId: Map<String, Concept> = emptyMap()
    private var native: AppLanguage = AppLanguage.En
    private var required: Int = UnlockGate.DEFAULT_CORRECT
    private var lastId: String? = null
    private var advance: Job? = null

    init {
        viewModelScope.launch {
            val loaded = runCatching { load() }
            if (loaded.isFailure) {
                session.finishCycle()
                _state.update { it.copy(ready = true, empty = true) }
            }
        }
    }

    private suspend fun load() {
        val profile = repo.activeProfile()
        concepts = repo.concepts()
        byId = concepts.associateBy { it.id }
        pool = repo.unlockPool()
        required = store.snapshot().requiredCorrect
        native = profile?.nativeLang ?: AppLanguage.En
        if (profile == null || pool.isEmpty()) {
            session.finishCycle()
            _state.update { it.copy(ready = true, empty = true, required = required) }
            return
        }
        showNext()
    }

    fun pick(option: String) {
        val current = _state.value
        if (!current.ready || current.revealed || current.passed || current.empty) return
        clicks.play()
        val ok = AnswerEvaluator.textMatches(current.correct, option, current.answerLang)
        val progress = UnlockProgress(current.required, current.score).answer(ok)
        _state.update {
            it.copy(
                picked = option,
                revealed = true,
                lastCorrect = ok,
                score = progress.correct,
                passed = progress.passed,
            )
        }
        advance?.cancel()
        advance = viewModelScope.launch {
            if (progress.passed) {
                delay(PASSED_MS)
                session.finishCycle()
            } else {
                delay(if (ok) NEXT_OK_MS else NEXT_MISS_MS)
                showNext()
            }
        }
    }

    private fun showNext() {
        val review = UnlockGate.pick(pool, lastId, Random.Default)
        val concept = review?.let { byId[it.conceptId] }
        if (review == null || concept == null) {
            session.finishCycle()
            _state.update { it.copy(ready = true, empty = true) }
            return
        }
        lastId = review.conceptId
        val factory = ExerciseFactory { lang -> concepts.map { it.text(lang).text } }
        val bank = factory.cloze(concept, native, review.targetLang, System.nanoTime()).banks.first()
        val scoreNow = _state.value.score
        _state.update {
            it.copy(
                ready = true,
                empty = false,
                prompt = concept.text(native).text,
                promptLang = native,
                answerLang = bank.lang,
                options = bank.options,
                correct = bank.correct,
                picked = null,
                revealed = false,
                lastCorrect = null,
                score = scoreNow,
                required = required,
                passed = false,
            )
        }
    }

    private companion object {
        const val PASSED_MS = 300L
        const val NEXT_OK_MS = 350L
        const val NEXT_MISS_MS = 500L
    }
}
