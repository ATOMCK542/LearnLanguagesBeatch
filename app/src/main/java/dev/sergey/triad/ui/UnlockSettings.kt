package dev.sergey.triad.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.sergey.triad.R
import dev.sergey.triad.domain.UnlockGate
import dev.sergey.triad.unlock.UnlockPermissions

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UnlockSettings(state: MainUiState, vm: MainViewModel) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var resumeTick by remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) resumeTick++
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val overlayOn = remember(resumeTick) { UnlockPermissions.overlayEnabled(context) }
    val a11yOn = remember(resumeTick) { UnlockPermissions.accessibilityEnabled(context) }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.settings_unlock), modifier = Modifier.weight(1f))
        Switch(
            checked = state.unlockEnabled,
            onCheckedChange = { on ->
                vm.setUnlockEnabled(on)
                if (on) openMissingPermission(context, overlayOn, a11yOn)
            },
        )
    }
    Text(
        stringResource(R.string.settings_unlock_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (!state.unlockEnabled) return
    Text(stringResource(R.string.settings_unlock_count))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (UnlockGate.MIN_CORRECT..UnlockGate.MAX_CORRECT).forEach { count ->
            FilterChip(
                selected = count == state.unlockRequired,
                onClick = { vm.setUnlockRequired(count) },
                label = { Text(count.toString()) },
            )
        }
    }
    if (!overlayOn) {
        Text(
            stringResource(R.string.settings_unlock_overlay),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = { context.startActivity(UnlockPermissions.overlaySettings(context)) }) {
            Text(stringResource(R.string.settings_unlock_open_overlay))
        }
    }
    if (!a11yOn) {
        Text(
            stringResource(R.string.settings_unlock_a11y),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = { UnlockPermissions.openAccessibilitySettings(context) }) {
            Text(stringResource(R.string.settings_unlock_open_a11y))
        }
        Text(
            stringResource(R.string.settings_unlock_restricted),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun openMissingPermission(context: Context, overlayOn: Boolean, a11yOn: Boolean) {
    val intent = when {
        !overlayOn -> UnlockPermissions.overlaySettings(context)
        !a11yOn -> {
            UnlockPermissions.openAccessibilitySettings(context)
            return
        }
        else -> return
    }
    context.startActivity(intent)
}
