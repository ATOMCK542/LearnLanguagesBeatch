package dev.sergey.triad.data.unlock

import dev.sergey.triad.domain.UnlockAction
import dev.sergey.triad.domain.UnlockGate
import dev.sergey.triad.domain.UnlockSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class UnlockSession @Inject constructor() {
    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()
    private var snapshot = UnlockSnapshot()

    fun isLocked(): Boolean = synchronized(this) { snapshot.locked }

    fun isRunning(): Boolean = _running.value

    fun attach(keyguardLocked: Boolean) = synchronized(this) {
        snapshot = if (keyguardLocked) {
            UnlockSnapshot(locked = true, passed = false)
        } else {
            UnlockSnapshot(locked = false, passed = true)
        }
        if (keyguardLocked) _running.value = false
    }

    fun onDevice(
        keyguardLocked: Boolean,
        foregroundPackage: String?,
        foregroundClass: String?,
        ownPackage: String,
        enabled: Boolean,
        hasPool: Boolean,
    ): UnlockAction = synchronized(this) {
        val (next, action) = UnlockGate.decide(
            snapshot,
            keyguardLocked,
            foregroundPackage,
            foregroundClass,
            ownPackage,
            enabled,
            hasPool,
        )
        snapshot = next
        _running.value = when (action) {
            UnlockAction.Show -> true
            UnlockAction.Dismiss -> false
            UnlockAction.Ignore -> _running.value
        }
        action
    }

    fun finishCycle() = synchronized(this) {
        if (snapshot.locked) {
            _running.value = false
            return
        }
        snapshot = UnlockSnapshot(locked = false, passed = true)
        _running.value = false
    }
}
