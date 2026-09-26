package dev.sergey.triad.unlock

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object UnlockPermissions {
    fun overlayEnabled(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun accessibilityEnabled(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        val flat = ComponentName(context, UnlockAccessibilityService::class.java).flattenToString()
        return enabled.split(':').any { it.equals(flat, ignoreCase = true) }
    }

    fun overlaySettings(context: Context): Intent =
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        )

    fun openAccessibilitySettings(context: Context) {
        val component = ComponentName(context, UnlockAccessibilityService::class.java).flattenToString()
        val details = Intent(ACTION_ACCESSIBILITY_DETAILS).apply {
            putExtra(Intent.EXTRA_COMPONENT_NAME, component)
        }
        val opened = runCatching { context.startActivity(details) }.isSuccess
        if (!opened) {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private const val ACTION_ACCESSIBILITY_DETAILS = "android.settings.ACCESSIBILITY_DETAILS_SETTINGS"
}
