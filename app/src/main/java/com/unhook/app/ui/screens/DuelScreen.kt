// UnHook — Duel screen showing weekly battle status and chore/wish navigation
package com.unhook.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unhook.app.R
import com.unhook.app.data.model.Partner
import com.unhook.app.data.model.User
import com.unhook.app.data.repository.UserRepository
import com.unhook.app.ui.theme.pressScale
import com.unhook.app.ui.theme.rememberReducedMotion

@Composable
fun DuelScreen(
    userRepository: UserRepository,
    onNavigateToChoreWish: () -> Unit,
) {
    val user by userRepository.getMe().collectAsState(initial = null)
    val partner by userRepository.getPartner().collectAsState(initial = null)
    val reducedMotion = rememberReducedMotion()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.duel_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.duel_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        BattleCard(user = user, partner = partner, reducedMotion = reducedMotion)

        Spacer(modifier = Modifier.height(24.dp))

        PointsBreakdownCard(user = user, reducedMotion = reducedMotion)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateToChoreWish,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(stringResource(R.string.duel_manage_chores_wishes))
        }

        Spacer(modifier = Modifier.height(12.dp))

        RulesCard()
    }
}

@Composable
private fun BattleCard(user: User?, partner: Partner?, reducedMotion: Boolean) {
    val myPoints = user?.weeklyPoints ?: 200
    val partnerPoints = partner?.weeklyPoints ?: 200
    val iAmWinning = myPoints > partnerPoints
    val isTied = myPoints == partnerPoints

    val animatedMyPoints by animateIntAsState(
        targetValue = myPoints,
        animationSpec = if (reducedMotion) snap() else tween(800, easing = FastOutSlowInEasing),
        label = "myScore",
    )
    val animatedPartnerPoints by animateIntAsState(
        targetValue = partnerPoints,
        animationSpec = if (reducedMotion) snap() else tween(800, easing = FastOutSlowInEasing),
        label = "partnerScore",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(reducedMotion),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BattlePlayerColumn(
                    emoji = user?.emojiAvatar ?: "😊",
                    name = user?.name ?: "You",
                    animatedPoints = animatedMyPoints,
                    isWinning = iAmWinning,
                    reducedMotion = reducedMotion,
                )

                Text(
                    text = stringResource(R.string.dashboard_vs),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )

                BattlePlayerColumn(
                    emoji = partner?.emojiAvatar ?: "❓",
                    name = partner?.name ?: "Partner",
                    animatedPoints = animatedPartnerPoints,
                    isWinning = !iAmWinning && !isTied,
                    reducedMotion = reducedMotion,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when {
                    isTied -> stringResource(R.string.duel_tied)
                    iAmWinning -> stringResource(R.string.duel_you_winning)
                    else -> stringResource(R.string.duel_partner_winning, partner?.name ?: "Partner")
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BattlePlayerColumn(
    emoji: String,
    name: String,
    animatedPoints: Int,
    isWinning: Boolean,
    reducedMotion: Boolean,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "winPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isWinning && !reducedMotion) 0.6f else 1f,
        targetValue = if (isWinning && !reducedMotion) 1.0f else 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "winAlpha",
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.height(28.dp), contentAlignment = Alignment.Center) {
            if (isWinning) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = pulseAlpha),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = stringResource(R.string.cd_winning),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Text(emoji, fontSize = 48.sp)
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = "$animatedPoints",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun PointsBreakdownCard(user: User?, reducedMotion: Boolean) {
    val animatedWeeklyPoints by animateIntAsState(
        targetValue = user?.weeklyPoints ?: 200,
        animationSpec = if (reducedMotion) snap() else tween(800, easing = FastOutSlowInEasing),
        label = "weeklyPoints",
    )
    val animatedResists by animateIntAsState(
        targetValue = user?.totalResists ?: 0,
        animationSpec = if (reducedMotion) snap() else tween(800, easing = FastOutSlowInEasing),
        label = "totalResists",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(reducedMotion),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.duel_your_stats),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))

            StatRow(stringResource(R.string.duel_weekly_points), "$animatedWeeklyPoints")
            StatRow(stringResource(R.string.duel_total_resists), "$animatedResists")
            StatRow(stringResource(R.string.duel_current_streak), "${user?.currentStreak ?: 0} days")
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun RulesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.duel_rules_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.duel_rules_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}
