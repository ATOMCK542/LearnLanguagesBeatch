package dev.sergey.triad.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Seed = Color(0xFF8B7CB8)
private val LightSurface = Color(0xFFF7F3FB)
private val LightBackground = Color(0xFFFBF8FD)

private val LightColors = lightColorScheme(
    primary = Seed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4D7F5),
    onPrimaryContainer = Color(0xFF2E2150),
    secondary = Color(0xFF9A86C4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE4F8),
    onSecondaryContainer = Color(0xFF33264F),
    tertiary = Color(0xFF7A6A9E),
    background = LightBackground,
    onBackground = Color(0xFF1C1628),
    surface = LightSurface,
    onSurface = Color(0xFF1C1628),
    surfaceVariant = Color(0xFFEDE6F4),
    onSurfaceVariant = Color(0xFF4B4458),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD4C4F0),
    onPrimary = Color(0xFF2E2150),
    primaryContainer = Color(0xFF5C4E86),
    onPrimaryContainer = Color(0xFFF1E8FF),
    secondary = Color(0xFFC9B8E6),
    background = Color(0xFF1A1524),
    onBackground = Color(0xFFF3ECF8),
    surface = Color(0xFF221C2E),
    onSurface = Color(0xFFF3ECF8),
    surfaceVariant = Color(0xFF3A3148),
    onSurfaceVariant = Color(0xFFD8CFE6),
)

data class LessonPalette(
    val correctContainer: Color,
    val onCorrect: Color,
    val wrongContainer: Color,
    val onWrong: Color,
)

@Composable
fun lessonPalette(): LessonPalette {
    return if (isSystemInDarkTheme()) {
        LessonPalette(
            correctContainer = Color(0xFF1F4D32),
            onCorrect = Color(0xFFB8F0C8),
            wrongContainer = Color(0xFF5C1F1F),
            onWrong = Color(0xFFFFC9C9),
        )
    } else {
        LessonPalette(
            correctContainer = Color(0xFFCDEDC8),
            onCorrect = Color(0xFF14532D),
            wrongContainer = Color(0xFFF8D0D0),
            onWrong = Color(0xFF7F1D1D),
        )
    }
}

@Composable
fun TriadTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content,
    )
}
