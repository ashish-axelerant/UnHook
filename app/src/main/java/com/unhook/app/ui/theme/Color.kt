// UnHook — Brand color palette with WCAG AA-compliant light/dark variants
package com.unhook.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Primary — Warm coral/salmon
val CoralLight = Color(0xFFFF7B6B)
val Coral = Color(0xFFE85D4A)
val CoralDark = Color(0xFFB8473A)

// Secondary — Deep teal
val TealLight = Color(0xFF4DB6AC)
val Teal = Color(0xFF00897B)
val TealDark = Color(0xFF005F56)

// Tertiary — Amber/gold for points and achievements
val AmberLight = Color(0xFFFFD54F)
val Amber = Color(0xFFFFC107)
val AmberDark = Color(0xFFC79100)

// Neutrals
val Surface = Color(0xFFFFF8F6)
val SurfaceDark = Color(0xFF1C1B1F)
val OnSurfaceLight = Color(0xFF1C1B1F)
val OnSurfaceDark = Color(0xFFE6E1E5)

// Semantic
val PointsGreen = Color(0xFF4CAF50)
val PointsRed = Color(0xFFEF5350)

// --- Light-mode safe colors (WCAG AA compliant) ---
// CoralDeep: 5.00:1 on white/warm surface — replaces Coral for text/buttons
val CoralDeep = Color(0xFFB8473A)
// CoralContainer: soft tonal pink for card/container backgrounds
val CoralContainer = Color(0xFFFFDAD6)
// OnAmberContainer: warm brown, 6.60:1 on AmberLight — for icons/text on amber bg
val OnAmberContainer = Color(0xFF5D4037)

// Semantic — light mode (original greens/reds are too light on white surface)
val PointsGreenDark = Color(0xFF2E7D32)  // 4.89:1 on #FFF8F6
val PointsRedDark = Color(0xFFD32F2F)    // 4.74:1 on #FFF8F6

// --- Dark-mode specific ---
// SurfaceDark (0xFF1C1B1F) on CoralLight = 6.77:1
val CoralDarkOnPrimary = Color(0xFF1C1B1F)
val DarkPrimaryContainer = Color(0xFF93000A)
val DarkOnPrimaryContainer = Color(0xFFFFDAD6)
val DarkOnTertiary = Color(0xFF4A3800)
val DarkTertiaryContainer = Color(0xFF6B5000)

// --- Composable helpers for adaptive semantic colors ---
@Composable
fun pointsPositiveColor() = if (isSystemInDarkTheme()) PointsGreen else PointsGreenDark

@Composable
fun pointsNegativeColor() = if (isSystemInDarkTheme()) PointsRed else PointsRedDark
