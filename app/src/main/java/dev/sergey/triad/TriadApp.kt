package dev.sergey.triad

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.sergey.triad.data.reminder.DailyReminder
import javax.inject.Inject

@HiltAndroidApp
class TriadApp : Application() {
    @Inject lateinit var reminders: DailyReminder

    override fun onCreate() {
        super.onCreate()
        reminders.ensureScheduled()
    }
}
