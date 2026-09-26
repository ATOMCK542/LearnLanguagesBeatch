package dev.sergey.triad.unlock

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.sergey.triad.R
import dev.sergey.triad.data.unlock.UnlockSession
import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.ui.theme.TriadTheme
import dev.sergey.triad.ui.theme.lessonPalette
import dev.sergey.triad.ui.theme.triadCardColors
import javax.inject.Inject

@AndroidEntryPoint
class UnlockActivity : AppCompatActivity() {
    @Inject lateinit var session: UnlockSession

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = Unit
            },
        )
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (!session.isRunning()) {
            finish()
            return
        }
        setContent {
            TriadTheme {
                val viewModel: UnlockViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    session.running.collect { open ->
                        if (!open) finish()
                    }
                }
                UnlockQuiz(state, viewModel::pick)
            }
        }
    }
}

@Composable
private fun UnlockQuiz(state: UnlockUiState, onPick: (String) -> Unit) {
    if (!state.ready || state.empty) {
        LinearProgressIndicator(Modifier.fillMaxWidth().safeDrawingPadding())
        return
    }
    val palette = lessonPalette()
    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.unlock_progress, state.score, state.required),
            style = MaterialTheme.typography.titleMedium,
        )
        state.lastCorrect?.let { ok ->
            if (state.revealed) {
                Text(
                    stringResource(if (ok) R.string.correct else R.string.incorrect),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (ok) palette.onCorrect else palette.onWrong,
                )
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), colors = triadCardColors()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    endonym(state.promptLang),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    state.prompt,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
        Text(
            stringResource(R.string.pick_in_language, endonym(state.answerLang)),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        )
        state.options.forEach { option ->
            val isCorrect = state.revealed && option == state.correct
            val isWrongPick = state.revealed && option == state.picked && option != state.correct
            val colors = when {
                isCorrect -> ButtonDefaults.filledTonalButtonColors(
                    containerColor = palette.correctContainer,
                    contentColor = palette.onCorrect,
                    disabledContainerColor = palette.correctContainer,
                    disabledContentColor = palette.onCorrect,
                )
                isWrongPick -> ButtonDefaults.filledTonalButtonColors(
                    containerColor = palette.wrongContainer,
                    contentColor = palette.onWrong,
                    disabledContainerColor = palette.wrongContainer,
                    disabledContentColor = palette.onWrong,
                )
                else -> ButtonDefaults.filledTonalButtonColors()
            }
            FilledTonalButton(
                onClick = { onPick(option) },
                enabled = !state.revealed,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                colors = colors,
            ) {
                Text(option, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun endonym(lang: AppLanguage): String = stringResource(
    when (lang) {
        AppLanguage.En -> R.string.lang_en_endonym
        AppLanguage.Ru -> R.string.lang_ru_endonym
        AppLanguage.Vi -> R.string.lang_vi_endonym
    },
)
