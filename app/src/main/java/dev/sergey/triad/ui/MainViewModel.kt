package dev.sergey.triad.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sergey.triad.data.audio.UiClickPlayer
import dev.sergey.triad.data.locale.AppLocale
import dev.sergey.triad.data.reminder.DailyReminder
import dev.sergey.triad.data.repo.TriadRepository
import dev.sergey.triad.data.unlock.UnlockGateStore
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
import dev.sergey.triad.domain.SessionItem
import dev.sergey.triad.domain.ThemeStudy
import dev.sergey.triad.domain.Theme
import dev.sergey.triad.domain.UnlockGate
import dev.sergey.triad.domain.WidgetKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
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
    val themeStudy: Map<String, ThemeStudy> = emptyMap(),
    val practiceAvailable: Int = 0,
    val phrasesReady: Int = 0,
    val practiceSize: Int = 20,
    val sessionPractice: Boolean = false,
    val selectedThemeId: String? = null,
    val planningSession: Boolean = false,
    val reminderEnabled: Boolean = true,
    val reminderPrompted: Boolean = false,
    val reminderReady: Boolean = false,
    val reminderTime: LocalTime = LocalTime.of(19, 0),
    val unlockEnabled: Boolean = false,
    val unlockRequired: Int = UnlockGate.DEFAULT_CORRECT,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val repo: TriadRepository,
    val tts: TtsController,
    val translator: TranslationEngine,
    private val clicks: UiClickPlayer,
    private val reminders: DailyReminder,
    private val unlockGate: UnlockGateStore,
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
            reminders.prefs.collect { prefs ->
                _state.update {
                    it.copy(
                        reminderEnabled = prefs.enabled,
                        reminderPrompted = prefs.prompted,
                        reminderReady = true,
                        reminderTime = prefs.time,
                    )
                }
            }
        }
        viewModelScope.launch {
            unlockGate.prefs.collect { prefs ->
                _state.update {
                    it.copy(
                        unlockEnabled = prefs.enabled,
                        unlockRequired = prefs.requiredCorrect,
                    )
                }
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
                val themeStudy = if (active != null) repo.themeStudy() else emptyMap()
                val practiceAvailable = repo.practicePoolSize()
                val phrasesReady = repo.phraseReadyCount()
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
                        themeStudy = themeStudy,
                        practiceAvailable = practiceAvailable,
                        phrasesReady = phrasesReady,
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

    fun startPhraseSession(onReady: () -> Unit = {}) {
        viewModelScope.launch {
            _state.update { it.copy(planningSession = true) }
            val plan = repo.planPhraseSession()
            _state.update {
                it.copy(
                    session = plan.items,
                    sessionIndex = 0,
                    revealed = false,
                    lastCorrect = null,
                    lastPicked = null,
                    quizPicks = emptyMap(),
                    sessionPractice = false,
                    planningSession = false,
                    phrasesReady = repo.phraseReadyCount(),
                    due = repo.dueCount(),
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

    fun startWidgetSession(kind: WidgetKind) {
        _state.update {
            it.copy(
                planningSession = true,
                session = emptyList(),
                sessionIndex = 0,
                revealed = false,
                lastCorrect = null,
                lastPicked = null,
                quizPicks = emptyMap(),
                sessionPractice = kind == WidgetKind.Review,
            )
        }
        viewModelScope.launch {
            val plan = when (kind) {
                WidgetKind.Lesson -> repo.planSession(sessionSize = WidgetKind.SESSION_SIZE)
                WidgetKind.Review -> repo.planPractice(WidgetKind.SESSION_SIZE)
            }
            _state.update {
                it.copy(
                    session = plan.items,
                    sessionIndex = 0,
                    revealed = false,
                    lastCorrect = null,
                    lastPicked = null,
                    quizPicks = emptyMap(),
                    sessionPractice = kind == WidgetKind.Review,
                    planningSession = false,
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
                repo.gradeLesson(item.reviews, results)
            }
            _state.update {
                it.copy(
                    quizPicks = picks,
                    revealed = true,
                    lastCorrect = results.values.all { ok -> ok },
                )
            }
            refreshStudyCounts()
        }
    }

    fun answerGame(correct: Boolean, picked: String? = null) {
        val item = currentItem() ?: return
        clicks.play()
        viewModelScope.launch {
            if (!_state.value.sessionPractice) {
                repo.gradeLesson(item.reviews, mapOf(item.exercise.targetLang to correct))
            }
            _state.update { it.copy(revealed = true, lastCorrect = correct, lastPicked = picked) }
            refreshStudyCounts()
        }
    }

    fun next() {
        val snapshot = _state.value
        val finishingStudy = !snapshot.sessionPractice &&
            snapshot.session.isNotEmpty() &&
            snapshot.sessionIndex + 1 >= snapshot.session.size
        val finished = if (finishingStudy) snapshot.session else emptyList()
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
            if (finished.isNotEmpty()) repo.enrollSession(finished)
            refreshStudyCounts()
        }
    }

    private suspend fun refreshStudyCounts() {
        val available = repo.practicePoolSize()
        val sizes = PracticePlanner.sizeChoices(available)
        val study = repo.themeStudy()
        _state.update {
            val size = if (it.practiceSize in sizes) it.practiceSize else sizes.lastOrNull() ?: 0
            it.copy(
                due = repo.dueCount(),
                practiceAvailable = available,
                phrasesReady = repo.phraseReadyCount(),
                practiceSize = size,
                themeStudy = study,
            )
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

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch { reminders.setEnabled(enabled) }
    }

    fun setReminderTime(time: LocalTime) {
        viewModelScope.launch { reminders.setTime(time) }
    }

    fun markReminderPrompted() {
        _state.update { it.copy(reminderPrompted = true) }
        viewModelScope.launch { reminders.markPrompted() }
    }

    fun setUnlockEnabled(enabled: Boolean) {
        viewModelScope.launch { unlockGate.setEnabled(enabled) }
    }

    fun setUnlockRequired(count: Int) {
        viewModelScope.launch { unlockGate.setRequiredCorrect(count) }
    }
}
