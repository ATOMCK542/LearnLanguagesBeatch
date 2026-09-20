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
import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.Concept
import dev.sergey.triad.domain.ConceptKind
import dev.sergey.triad.domain.Exercise
import dev.sergey.triad.domain.LocalizedText
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
    val query: String = "",
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
                _state.update {
                    it.copy(
                        ready = true,
                        profiles = profiles,
                        active = active,
                        due = repo.dueCount(),
                        themes = themes,
                        concepts = concepts,
                        unlocked = unlocked,
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

    fun startSession() {
        viewModelScope.launch {
            val plan = repo.planSession()
            _state.update {
                it.copy(
                    session = plan.items,
                    sessionIndex = 0,
                    revealed = false,
                    lastCorrect = null,
                    lastPicked = null,
                    due = repo.dueCount(),
                )
            }
        }
    }

    fun reveal() {
        _state.update { it.copy(revealed = true) }
    }

    fun playClick() {
        clicks.play()
    }

    fun answerGame(correct: Boolean, picked: String? = null) {
        val item = currentItem() ?: return
        clicks.play()
        viewModelScope.launch {
            repo.applyRating(item.review, if (correct) Rating.Good else Rating.Again)
            _state.update { it.copy(revealed = true, lastCorrect = correct, lastPicked = picked) }
        }
    }

    fun rate(rating: Rating) {
        val item = currentItem() ?: return
        clicks.play()
        viewModelScope.launch {
            repo.applyRating(item.review, rating)
            next()
        }
    }

    fun next() {
        _state.update {
            val nextIndex = it.sessionIndex + 1
            it.copy(sessionIndex = nextIndex, revealed = false, lastCorrect = null, lastPicked = null)
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

    fun speak(text: String, lang: AppLanguage): Boolean = tts.speak(text, lang)

    fun ttsAvailable(lang: AppLanguage) = tts.available(lang)
}
