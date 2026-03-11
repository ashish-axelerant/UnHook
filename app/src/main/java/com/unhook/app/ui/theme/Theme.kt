// UnHook — Material 3 theme with light and dark color schemes
package com.unhook.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Coral,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = CoralLight,
    onPrimaryContainer = CoralDark,
    secondary = Teal,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = TealLight,
    onSecondaryContainer = TealDark,
    tertiary = Amber,
    onTertiary = androidx.compose.ui.graphics.Color.Black,
    tertiaryContainer = AmberLight,
    onTertiaryContainer = AmberDark,
    surface = Surface,
    onSurface = OnSurfaceLight,
    background = Surface,
    onBackground = OnSurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = CoralLight,
    onPrimary = CoralDark,
    primaryContainer = CoralDark,
    onPrimaryContainer = CoralLight,
    secondary = TealLight,
    onSecondary = TealDark,
    secondaryContainer = TealDark,
    onSecondaryContainer = TealLight,
    tertiary = AmberLight,
    onTertiary = AmberDark,
    tertiaryContainer = AmberDark,
    onTertiaryContainer = AmberLight,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
)

@Composable
fun UnHookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = UnHookTypography,
        content = content,
    )
}
