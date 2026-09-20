package dev.sergey.triad.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sergey.triad.data.audio.UiClickPlayer
import dev.sergey.triad.data.locale.AppLocale
import dev.sergey.triad.data.repo.TriadRepository
import dev.sergey.triad.data.translate.TranslationEngine
import dev.sergey.triad.data.tts.TtsController
import dev.sergey.triad.domain.AnswerEvaluator
import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.Concept
import dev.sergey.triad.domain.ConceptKind
import dev.sergey.triad.domain.Exercise
import dev.sergey.triad.domain.LocalizedText
import dev.sergey.triad.domain.PracticePlanner
import dev.sergey.triad.domain.Profile
import dev.sergey.triad.domain.Rating
import dev.sergey.triad.domain.SessionItem
import dev.sergey.triad.domain.Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val ready: Boolean = false,
    val profiles: List<Profile> = emptyList(),
    val active: Profile? = null,
    val due: Int = 0,
    val themes: List<Theme> = emptyList(),
    val concepts: List<Concept> = emptyList(),
    val unlocked: Set<String> = emptySet(),
    val session: List<SessionItem> = emptyList(),
    val sessionIndex: Int = 0,
    val revealed: Boolean = false,
    val lastCorrect: Boolean? = null,
    val lastPicked: String? = null,
    val quizPicks: Map<AppLanguage, String> = emptyMap(),
    val query: String = "",
    val ttsVoices: Set<AppLanguage> = emptySet(),
    val masteredIds: Set<String> = emptySet(),
    val practiceAvailable: Int = 0,
    val practiceSize: Int = 20,
    val sessionPractice: Boolean = false,
    val selectedThemeId: String? = null,
    val planningSession: Boolean = false,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val repo: TriadRepository,
    val tts: TtsController,
    val translator: TranslationEngine,
    private val clicks: UiClickPlayer,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            tts.status.collect { st ->
                _state.update { it.copy(ttsVoices = st.voices) }
            }
        }
        viewModelScope.launch {
            repo.bootstrap()
            combine(repo.profiles, repo.activeProfileId) { profiles, activeId ->
                profiles to (activeId?.let { id -> profiles.firstOrNull { it.id == id } } ?: profiles.firstOrNull())
            }.collect { (profiles, active) ->
                if (active != null) {
                    AppLocale.apply(getApplication(), active.uiLang)
                }
                val themes = repo.themes()
                val concepts = repo.concepts()
                val unlocked = active?.let { repo.pathState(it).filter { e -> e.value.unlocked }.keys } ?: emptySet()
                val mastered = active?.let { repo.masteredConceptIds(it.id) } ?: emptySet()
                val practiceAvailable = repo.practicePoolSize()
                val sizes = PracticePlanner.sizeChoices(practiceAvailable)
                val practiceSize = _state.value.practiceSize.let { current ->
                    if (current in sizes) current else sizes.lastOrNull() ?: 0
                }
                _state.update {
                    it.copy(
                        ready = true,
                        profiles = profiles,
                        active = active,
                        due = repo.dueCount(),
                        themes = themes,
                        concepts = concepts,
                        unlocked = unlocked,
                        masteredIds = mastered,
                        practiceAvailable = practiceAvailable,
                        practiceSize = practiceSize,
                    )
                }
            }
        }
    }

    fun createProfile(name: String, email: String?, ui: AppLanguage, targets: List<AppLanguage>) {
        viewModelScope.launch {
            repo.createProfile(name, email, ui, ui, targets)
        }
    }

    fun switchProfile(id: Long) {
        viewModelScope.launch { repo.switchProfile(id) }
    }

    fun deleteProfile(id: Long) {
        viewModelScope.launch { repo.deleteProfile(id) }
    }

    fun updateActive(transform: (Profile) -> Profile) {
        viewModelScope.launch {
            val current = repo.activeProfile() ?: return@launch
            val updated = transform(current)
            repo.updateProfile(updated)
            AppLocale.apply(getApplication(), updated.uiLang)
        }
    }

    fun startSession(onReady: () -> Unit = {}) {
        viewModelScope.launch {
            _state.update { it.copy(planningSession = true) }
            val themeId = _state.value.selectedThemeId
            if (themeId != null) repo.unlockTheme(themeId)
            val plan = repo.planSession(preferredThemeId = themeId)
            val unlocked = _state.value.active?.let { repo.pathState(it).filter { e -> e.value.unlocked }.keys }
                ?: _state.value.unlocked
            _state.update {
                it.copy(
                    session = plan.items,
                    sessionIndex = 0,
                    revealed = false,
                    lastCorrect = null,
                    lastPicked = null,
                    quizPicks = emptyMap(),
                    due = repo.dueCount(),
                    sessionPractice = false,
                    planningSession = false,
                    unlocked = unlocked,
                )
            }
            onReady()
        }
    }

    fun setPracticeSize(size: Int) {
        _state.update { it.copy(practiceSize = size) }
    }

    fun startPractice() {
        viewModelScope.launch {
            val size = _state.value.practiceSize
            val plan = repo.planPractice(size)
            _state.update {
                it.copy(
                    session = plan.items,
                    sessionIndex = 0,
                    revealed = false,
                    lastCorrect = null,
                    lastPicked = null,
                    quizPicks = emptyMap(),
                    sessionPractice = true,
                    due = repo.dueCount(),
                )
            }
        }
    }

    fun toggleMastered(conceptId: String) {
        viewModelScope.launch {
            val next = conceptId !in _state.value.masteredIds
            repo.setMastered(conceptId, next)
            val mastered = repo.activeProfile()?.let { repo.masteredConceptIds(it.id) } ?: emptySet()
            val available = repo.practicePoolSize()
            val sizes = PracticePlanner.sizeChoices(available)
            val size = _state.value.practiceSize.let { current ->
                if (current in sizes) current else sizes.lastOrNull() ?: 0
            }
            _state.update {
                it.copy(masteredIds = mastered, practiceAvailable = available, practiceSize = size)
            }
        }
    }

    fun selectTheme(themeId: String) {
        setThemeStudying(themeId, true)
    }

    fun setThemeStudying(themeId: String, studying: Boolean) {
        if (!studying) {
            _state.update { it.copy(selectedThemeId = it.selectedThemeId.takeUnless { id -> id == themeId }) }
            return
        }
        _state.update { it.copy(selectedThemeId = themeId) }
        viewModelScope.launch {
            repo.unlockTheme(themeId)
            val unlocked = _state.value.active?.let { repo.pathState(it).filter { e -> e.value.unlocked }.keys }
                ?: emptySet()
            _state.update { it.copy(unlocked = unlocked) }
        }
    }

    fun playClick() {
        clicks.play()
    }

    fun pickQuiz(lang: AppLanguage, option: String) {
        val item = currentItem() ?: return
        val banks = (item.exercise as? Exercise.Cloze)?.banks ?: return
        clicks.play()
        val picks = _state.value.quizPicks + (lang to option)
        if (banks.any { it.lang !in picks }) {
            _state.update { it.copy(quizPicks = picks) }
            return
        }
        viewModelScope.launch {
            val results = banks.associate { bank ->
                val picked = picks[bank.lang].orEmpty()
                bank.lang to AnswerEvaluator.textMatches(bank.correct, picked, bank.lang)
            }
            if (!_state.value.sessionPractice) {
                item.reviews.forEach { review ->
                    val ok = results[review.targetLang] ?: return@forEach
                    repo.applyRating(review, if (ok) Rating.Good else Rating.Again)
                }
            }
            _state.update {
                it.copy(
                    quizPicks = picks,
                    revealed = true,
                    lastCorrect = results.values.all { ok -> ok },
                )
            }
        }
    }

    fun answerGame(correct: Boolean, picked: String? = null) {
        val item = currentItem() ?: return
        clicks.play()
        viewModelScope.launch {
            if (!_state.value.sessionPractice) {
                repo.applyRating(item.review, if (correct) Rating.Good else Rating.Again)
            }
            _state.update { it.copy(revealed = true, lastCorrect = correct, lastPicked = picked) }
        }
    }

    fun next() {
        _state.update {
            it.copy(
                sessionIndex = it.sessionIndex + 1,
                revealed = false,
                lastCorrect = null,
                lastPicked = null,
                quizPicks = emptyMap(),
            )
        }
        viewModelScope.launch {
            _state.update { it.copy(due = repo.dueCount()) }
        }
    }

    fun currentItem(): SessionItem? {
        val s = _state.value
        return s.session.getOrNull(s.sessionIndex)
    }

    fun completePrimer(id: String) {
        viewModelScope.launch { repo.completePrimer(id) }
    }

    fun setQuery(value: String) {
        _state.update { it.copy(query = value) }
    }

    fun addCard(en: String, ru: String, vi: String) {
        viewModelScope.launch {
            repo.addUserConcept(
                ConceptKind.Phrase,
                mapOf(
                    AppLanguage.En to en,
                    AppLanguage.Ru to ru,
                    AppLanguage.Vi to vi,
                ),
                LocalizedText("", "", ""),
            )
            _state.update { it.copy(concepts = repo.concepts()) }
        }
    }

    fun export(all: Boolean, onText: (String) -> Unit) {
        viewModelScope.launch { onText(repo.exportJson(all)) }
    }

    fun importFrom(uri: Uri) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val text = app.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: return@launch
            repo.importJson(text)
        }
    }

    fun writeExport(uri: Uri, body: String) {
        viewModelScope.launch {
            getApplication<Application>().contentResolver.openOutputStream(uri)?.use { it.write(body.toByteArray()) }
        }
    }

    fun speak(text: String, lang: AppLanguage, openSettingsIfMissing: Boolean = true): Boolean {
        if (tts.speak(text, lang)) return true
        if (openSettingsIfMissing && tts.status.value.ready && !tts.available(lang)) {
            tts.openVoiceSettings()
        }
        return false
    }

    fun ttsAvailable(lang: AppLanguage) = tts.available(lang)
}
