package dev.sergey.triad.unlock

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import dev.sergey.triad.data.repo.TriadRepository
import dev.sergey.triad.data.unlock.UnlockGateStore
import dev.sergey.triad.data.unlock.UnlockSession
import dev.sergey.triad.domain.UnlockAction
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@AndroidEntryPoint
class UnlockAccessibilityService : AccessibilityService() {
    @Inject lateinit var store: UnlockGateStore
    @Inject lateinit var repo: TriadRepository
    @Inject lateinit var session: UnlockSession

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val gate = Mutex()
    @Volatile private var enabled: Boolean = false
    private var hasPool: Boolean = false
    private var poolAt: Long = 0L

    private val keyguard by lazy { getSystemService(KeyguardManager::class.java) }
    private var receiversRegistered = false

    private val screen = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> evaluate(null, null, forceLocked = true)
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> evaluate(null, null)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        session.attach(keyguard.isKeyguardLocked)
        if (receiversRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screen, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screen, filter)
        }
        receiversRegistered = true
        scope.launch {
            store.prefs.collect { prefs ->
                val was = enabled
                enabled = prefs.enabled
                if (was && !prefs.enabled) evaluate(null, null)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        evaluate(event.packageName?.toString(), event.className?.toString())
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (receiversRegistered) {
            unregisterReceiver(screen)
            receiversRegistered = false
        }
        if (::session.isInitialized) session.finishCycle()
        scope.cancel()
        super.onDestroy()
    }

    private fun evaluate(packageName: String?, className: String?, forceLocked: Boolean = false) {
        scope.launch {
            gate.withLock {
                val locked = forceLocked || keyguard.isKeyguardLocked
                val pool = if (enabled && !locked) poolNonEmpty(session.isLocked()) else false
                val action = session.onDevice(
                    keyguardLocked = locked,
                    foregroundPackage = packageName,
                    foregroundClass = className,
                    ownPackage = this@UnlockAccessibilityService.packageName,
                    enabled = enabled,
                    hasPool = pool,
                )
                if (action == UnlockAction.Show && Settings.canDrawOverlays(this@UnlockAccessibilityService)) {
                    launchQuiz()
                }
            }
        }
    }

    private suspend fun poolNonEmpty(refresh: Boolean): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (refresh || now - poolAt > POOL_TTL_MS) {
            hasPool = runCatching { repo.unlockPool().isNotEmpty() }.getOrDefault(false)
            poolAt = now
        }
        return hasPool
    }

    private fun launchQuiz() {
        val intent = Intent(this, UnlockActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP,
            )
        }
        runCatching { startActivity(intent) }
    }

    private companion object {
        const val POOL_TTL_MS = 30_000L
    }
}
