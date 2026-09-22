package dev.sergey.triad.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import dev.sergey.triad.domain.WidgetKind

object WidgetPinner {
    fun supported(context: Context): Boolean =
        AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported

    fun request(context: Context, kind: WidgetKind): Boolean {
        val manager = AppWidgetManager.getInstance(context)
        if (!manager.isRequestPinAppWidgetSupported) return false
        val receiver = when (kind) {
            WidgetKind.Lesson -> LessonWidgetReceiver::class.java
            WidgetKind.Review -> ReviewWidgetReceiver::class.java
        }
        return manager.requestPinAppWidget(ComponentName(context, receiver), null, null)
    }
}
