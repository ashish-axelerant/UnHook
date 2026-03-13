// UnHook — Material 3 theme with WCAG AA-compliant light and dark color schemes
package com.unhook.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    // primary: CoralDeep gives 5.00:1 on white — old Coral was 3.28:1 FAIL
    primary = CoralDeep,
    onPrimary = Color.White,
    // primaryContainer: soft pink tonal bg — old CoralLight had 2.53:1 FAIL
    primaryContainer = CoralContainer,
    onPrimaryContainer = OnSurfaceLight,  // 13.26:1 on CoralContainer
    // secondary: TealDark gives 7.58:1 on white — old Teal was 4.32:1 FAIL
    secondary = TealDark,
    onSecondary = Color.White,
    secondaryContainer = TealLight,
    onSecondaryContainer = TealDark,
    tertiary = Amber,
    onTertiary = Color.Black,
    tertiaryContainer = AmberLight,
    // onTertiaryContainer: warm brown 6.60:1 on AmberLight — old AmberDark was 1.99:1 FAIL
    onTertiaryContainer = OnAmberContainer,
    surface = Surface,
    onSurface = OnSurfaceLight,
    background = Surface,
    onBackground = OnSurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = CoralLight,
    // onPrimary: SurfaceDark on CoralLight = 6.77:1 — old CoralDark was 2.07:1 FAIL
    onPrimary = CoralDarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = TealLight,
    onSecondary = TealDark,
    secondaryContainer = TealDark,
    onSecondaryContainer = TealLight,
    tertiary = AmberLight,
    // onTertiary: dark amber/brown passes on AmberLight
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    // onTertiaryContainer: AmberLight gives high contrast on dark amber bg
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
