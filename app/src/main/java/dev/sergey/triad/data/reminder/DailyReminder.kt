package dev.sergey.triad.data.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sergey.triad.MainActivity
import dev.sergey.triad.R
import dev.sergey.triad.data.repo.TriadRepository
import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.ReminderSchedule
import dev.sergey.triad.reminder.ReminderReceiver
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Singleton
class DailyReminder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val store: ReminderStore,
    private val repo: TriadRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val prefs: Flow<ReminderPrefs> = store.prefs

    fun ensureScheduled() {
        scope.launch { syncSchedule() }
    }

    fun onAppOpened() {
        scope.launch {
            store.markOpened(LocalDate.now().toString())
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
            syncSchedule()
        }
    }

    suspend fun setEnabled(enabled: Boolean) {
        store.setEnabled(enabled)
        if (enabled) schedule(ReminderSchedule.nextTrigger(ZonedDateTime.now())) else cancel()
    }

    suspend fun markPrompted() {
        store.markPrompted()
    }

    suspend fun onAlarm() {
        deliver(fromAlarm = true)
    }

    suspend fun onBoot() {
        deliver(fromAlarm = false)
    }

    private suspend fun syncSchedule() {
        if (store.snapshot().enabled) {
            schedule(ReminderSchedule.nextTrigger(ZonedDateTime.now()))
        } else {
            cancel()
        }
    }

    private suspend fun deliver(fromAlarm: Boolean) {
        val now = ZonedDateTime.now()
        val enabled = store.snapshot().enabled
        if (!enabled) {
            cancel()
            return
        }
        val decision = ReminderSchedule.decide(
            now = now,
            lastOpenDay = store.lastOpenDay(),
            lastNotifiedDay = store.lastNotifiedDay(),
            fromAlarm = fromAlarm,
        )
        if (decision.notify && canPost()) {
            show()
            store.markNotified(now.toLocalDate().toString())
        }
        schedule(decision.next)
    }

    private suspend fun show() {
        val profile = repo.activeProfile()
        val lang = profile?.uiLang ?: AppLanguage.entries.firstOrNull {
            it.code == Locale.getDefault().language
        } ?: AppLanguage.En
        val localized = localized(lang)
        val due = runCatching { repo.dueCount() }.getOrDefault(0)
        val body = if (due > 0) {
            localized.resources.getQuantityString(R.plurals.reminder_due, due, due)
        } else {
            localized.getString(R.string.reminder_body)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                localized.getString(R.string.reminder_channel),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_reminder)
            .setContentTitle(localized.getString(R.string.reminder_title))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun schedule(whenAt: ZonedDateTime) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        alarm.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            whenAt.toInstant().toEpochMilli(),
            reminderIntent(),
        )
    }

    private fun cancel() {
        context.getSystemService(AlarmManager::class.java)?.cancel(reminderIntent())
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun reminderIntent(): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).setAction(ReminderReceiver.ACTION_REMIND)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun canPost(): Boolean {
        if (Build.VERSION.SDK_INT < 33) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun localized(lang: AppLanguage): Context {
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.forLanguageTag(lang.code))
        return context.createConfigurationContext(config)
    }

    private companion object {
        const val CHANNEL_ID = "study_reminder"
        const val NOTIFICATION_ID = 1001
        const val REQUEST_CODE = 41
    }
}
