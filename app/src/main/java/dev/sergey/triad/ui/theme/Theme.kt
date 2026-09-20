package dev.sergey.triad.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Seed = Color(0xFF8B7CB8)
private val LightSurface = Color(0xFFF7F3FB)
private val LightBackground = Color(0xFFFBF8FD)
private val LightOn = Color(0xFF1C1628)

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
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOn,
    surface = LightSurface,
    onSurface = LightOn,
    surfaceVariant = Color(0xFFEDE6F4),
    onSurfaceVariant = Color(0xFF4B4458),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F3FB),
    surfaceContainer = Color(0xFFF1EAF7),
    surfaceContainerHigh = Color(0xFFEBE3F3),
    surfaceContainerHighest = Color(0xFFE4D7F5),
    outline = Color(0xFF7A7288),
    outlineVariant = Color(0xFFD4CCDE),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkOn = Color(0xFFF3ECF8)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD4C4F0),
    onPrimary = Color(0xFF2E2150),
    primaryContainer = Color(0xFF5C4E86),
    onPrimaryContainer = Color(0xFFF1E8FF),
    secondary = Color(0xFFC9B8E6),
    onSecondary = Color(0xFF2E2150),
    secondaryContainer = Color(0xFF4A3D68),
    onSecondaryContainer = Color(0xFFEDE4F8),
    tertiary = Color(0xFFC5B8E0),
    onTertiary = Color(0xFF2E2150),
    background = Color(0xFF1A1524),
    onBackground = DarkOn,
    surface = Color(0xFF221C2E),
    onSurface = DarkOn,
    surfaceVariant = Color(0xFF3A3148),
    onSurfaceVariant = Color(0xFFD8CFE6),
    surfaceContainerLowest = Color(0xFF140F1C),
    surfaceContainerLow = Color(0xFF1E1828),
    surfaceContainer = Color(0xFF221C2E),
    surfaceContainerHigh = Color(0xFF2C253A),
    surfaceContainerHighest = Color(0xFF372F48),
    outline = Color(0xFF9A8FB0),
    outlineVariant = Color(0xFF4A4158),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
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
fun triadCardColors() = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor = MaterialTheme.colorScheme.onSurface,
)

@Composable
fun TriadTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content,
    )
}
