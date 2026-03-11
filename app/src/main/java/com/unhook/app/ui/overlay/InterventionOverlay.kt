// UnHook — Intervention overlay screen with countdown, scores, and resist/let-me-in buttons
package com.unhook.app.ui.overlay

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unhook.app.R
import com.unhook.app.ui.theme.PointsGreen
import com.unhook.app.ui.theme.PointsRed

@Composable
fun InterventionOverlay(
    viewModel: InterventionViewModel,
) {
    val state by viewModel.uiState.collectAsState()
    val view = LocalView.current

    if (!state.isVisible) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.97f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // App name being blocked
            Text(
                text = stringResource(R.string.intervention_opening, state.appName),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // VS Score card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ScoreColumn(
                    emoji = state.myEmoji,
                    name = state.myName,
                    points = state.myPoints,
                    isWinning = state.myPoints > state.partnerPoints,
                )
                Text(
                    text = stringResource(R.string.dashboard_vs),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ScoreColumn(
                    emoji = state.partnerEmoji,
                    name = state.partnerName,
                    points = state.partnerPoints,
                    isWinning = state.partnerPoints > state.myPoints,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Breathing animation circle with countdown
            BreathingCircle(seconds = state.countdownSeconds)

            Spacer(modifier = Modifier.height(24.dp))

            // Reminder message
            Text(
                text = state.reminderMessage,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Resist button (always active)
            Button(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.onResist()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PointsGreen,
                ),
            ) {
                Text(
                    text = stringResource(R.string.intervention_resist),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Let me in button (active after countdown)
            OutlinedButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    viewModel.onLetMeIn()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canLetIn,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = if (state.canLetIn) {
                        stringResource(R.string.intervention_let_me_in)
                    } else {
                        stringResource(R.string.intervention_wait, state.countdownSeconds)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun ScoreColumn(
    emoji: String,
    name: String,
    points: Int,
    isWinning: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (isWinning) {
            Text(text = "👑", fontSize = 16.sp)
        }
        Text(text = emoji, fontSize = 36.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "$points",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.dashboard_points),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BreathingCircle(seconds: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathScale",
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (seconds > 0) {
            Text(
                text = "$seconds",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else {
            Text(
                text = "🧘",
                fontSize = 40.sp,
            )
        }
    }
}
