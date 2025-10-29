package com.example.geonote.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LavandaTech,
    onPrimary = SuperficieClara,
    primaryContainer = LavandaTech.copy(alpha = 0.1f),
    onPrimaryContainer = TextoPrincipalClaro,

    secondary = VerdeMenta,
    onSecondary = SuperficieClara,
    secondaryContainer = VerdeMenta.copy(alpha = 0.1f),
    onSecondaryContainer = TextoPrincipalClaro,

    tertiary = AcentoAmarillo,
    onTertiary = TextoPrincipalClaro,
    tertiaryContainer = AcentoAmarillo.copy(alpha = 0.1f),
    onTertiaryContainer = TextoPrincipalClaro,

    error = ErrorCoral,
    onError = SuperficieClara,
    errorContainer = ErrorCoral.copy(alpha = 0.1f),
    onErrorContainer = ErrorCoral,

    background = FondoClaro,
    onBackground = TextoPrincipalClaro,

    surface = SuperficieClara,
    onSurface = TextoPrincipalClaro,
    surfaceVariant = FondoClaro,
    onSurfaceVariant = TextoSecundarioClaro,

    outline = TextoSecundarioClaro.copy(alpha = 0.3f),
    outlineVariant = TextoSecundarioClaro.copy(alpha = 0.1f),
)

private val DarkColorScheme = darkColorScheme(
    primary = LavandaSuave,
    onPrimary = TextoPrincipalOscuro,
    primaryContainer = LavandaSuave.copy(alpha = 0.2f),
    onPrimaryContainer = TextoPrincipalOscuro,

    secondary = MentaSuave,
    onSecondary = TextoPrincipalOscuro,
    secondaryContainer = MentaSuave.copy(alpha = 0.2f),
    onSecondaryContainer = TextoPrincipalOscuro,

    tertiary = AcentoAmarilloSuave,
    onTertiary = FondoOscuro,
    tertiaryContainer = AcentoAmarilloSuave.copy(alpha = 0.2f),
    onTertiaryContainer = TextoPrincipalOscuro,

    error = ErrorCoral,
    onError = FondoOscuro,
    errorContainer = ErrorCoral.copy(alpha = 0.2f),
    onErrorContainer = ErrorCoral,

    background = FondoOscuro,
    onBackground = TextoPrincipalOscuro,

    surface = SuperficieOscura,
    onSurface = TextoPrincipalOscuro,
    surfaceVariant = SuperficieOscura,
    onSurfaceVariant = TextoSecundarioOscuro,

    outline = TextoSecundarioOscuro.copy(alpha = 0.3f),
    outlineVariant = TextoSecundarioOscuro.copy(alpha = 0.1f),
)

@Composable
fun GeoNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}