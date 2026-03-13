// UnHook — Shared motion utilities: reduced-motion check and press-scale modifier
package com.unhook.app.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext

/**
 * Returns true when the system "Animator duration scale" is set to 0
 * (i.e. the user has enabled "Remove animations" in Developer Options).
 * All animations should degrade to instant transitions when this is true.
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
}

/**
 * Adds a subtle 0.96 scale-down on press. No-ops when [reducedMotion] is true.
 * The clickable is indication-less — attach your own ripple/click handler separately;
 * this modifier only handles the visual scale feedback.
 */
fun Modifier.pressScale(reducedMotion: Boolean): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !reducedMotion) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "pressScale",
    )
    this
        .scale(scale)
        .clickable(interactionSource = interactionSource, indication = null) {}
}
