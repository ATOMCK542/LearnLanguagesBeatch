package dev.sergey.triad.ui.widget

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.sergey.triad.R
import dev.sergey.triad.data.reminder.DailyReminder
import dev.sergey.triad.data.widget.WidgetDayStore
import dev.sergey.triad.domain.WidgetKind
import dev.sergey.triad.ui.MainViewModel
import dev.sergey.triad.ui.SessionScreen
import dev.sergey.triad.ui.theme.TriadTheme
import javax.inject.Inject

@AndroidEntryPoint
class WidgetSessionActivity : AppCompatActivity() {
    @Inject lateinit var dayStore: WidgetDayStore
    @Inject lateinit var reminders: DailyReminder

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        val kind = WidgetKind.fromExtra(intent.getStringExtra(EXTRA_KIND))
        if (kind == null) {
            finish()
            return
        }
        reminders.onAppOpened()
        setContent {
            TriadTheme {
                CompactChrome {
                    WidgetSessionHost(
                        kind = kind,
                        dayStore = dayStore,
                        onClose = { finish() },
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_KIND = "widget_kind"
    }
}

@Composable
private fun CompactChrome(content: @Composable () -> Unit) {
    val base = MaterialTheme.typography
    val compactType = base.copy(
        headlineSmall = base.titleLarge,
        titleLarge = base.titleMedium,
        titleMedium = base.titleSmall,
        titleSmall = base.bodyLarge,
        bodyLarge = base.bodyMedium,
        bodyMedium = base.bodySmall,
    )
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.86f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 8.dp,
        ) {
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme,
                typography = compactType,
                content = content,
            )
        }
    }
}

private enum class WidgetPhase { Loading, Session, Done, NoProfile }

@Composable
private fun WidgetSessionHost(
    kind: WidgetKind,
    dayStore: WidgetDayStore,
    onClose: () -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var phase by remember { mutableStateOf(WidgetPhase.Loading) }
    var marked by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(state.ready) {
        if (!state.ready || phase != WidgetPhase.Loading) return@LaunchedEffect
        val profile = state.active
        if (profile == null) {
            phase = WidgetPhase.NoProfile
            return@LaunchedEffect
        }
        if (dayStore.isDone(profile.id, kind)) {
            phase = WidgetPhase.Done
            return@LaunchedEffect
        }
        phase = WidgetPhase.Session
        viewModel.startWidgetSession(kind)
    }

    LaunchedEffect(state.planningSession, state.sessionIndex, state.session.size) {
        val finished = phase == WidgetPhase.Session &&
            !state.planningSession &&
            state.session.isNotEmpty() &&
            state.sessionIndex >= state.session.size
        if (!finished || marked) return@LaunchedEffect
        val profileId = state.active?.id ?: return@LaunchedEffect
        marked = true
        dayStore.markDone(profileId, kind)
        LessonGlanceWidget().updateAll(context)
        ReviewGlanceWidget().updateAll(context)
    }

    when {
        !state.ready || phase == WidgetPhase.Loading || state.planningSession -> {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(24.dp))
        }
        phase == WidgetPhase.NoProfile -> {
            MessagePane(stringResource(R.string.widget_status_no_profile), onClose)
        }
        phase == WidgetPhase.Done -> {
            MessagePane(stringResource(R.string.widget_done_body), onClose)
        }
        else -> {
            SessionScreen(state, viewModel, compact = true, onClose = onClose)
        }
    }
}

@Composable
private fun MessagePane(text: String, onClose: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
        ) {
            Text(stringResource(R.string.widget_close))
        }
    }
}
