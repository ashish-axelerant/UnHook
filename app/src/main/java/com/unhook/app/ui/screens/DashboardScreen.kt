// UnHook — Dashboard with VS score card, stats row, activity feed, and fluid animations
package com.unhook.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unhook.app.R
import com.unhook.app.data.model.Partner
import com.unhook.app.data.model.PointEvent
import com.unhook.app.data.model.User
import com.unhook.app.data.repository.PointsRepository
import com.unhook.app.data.repository.UserRepository
import com.unhook.app.ui.theme.pointsNegativeColor
import com.unhook.app.ui.theme.pointsPositiveColor
import com.unhook.app.ui.theme.pressScale
import com.unhook.app.ui.theme.rememberReducedMotion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

private val motivationalMessages = listOf(
    "Every resist makes you stronger 💪",
    "You and your partner are a team 🤝",
    "Small wins lead to big changes ✨",
    "The scroll can wait. Life can't. 🌟",
    "Your future self thanks you 🙏",
    "Be the one with the crown today 👑",
    "Resist together, grow together 🌱",
)

@Composable
fun DashboardScreen(
    userRepository: UserRepository,
    pointsRepository: PointsRepository,
    onNavigateToReport: () -> Unit = {},
) {
    val user by userRepository.getMe().collectAsState(initial = null)
    val partner by userRepository.getPartner().collectAsState(initial = null)
    val recentEvents by (user?.let { pointsRepository.getRecentEvents(it.id) }
        ?: kotlinx.coroutines.flow.flowOf(emptyList())).collectAsState(initial = emptyList())

    val reducedMotion = rememberReducedMotion()

    val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
    val todayMessage = motivationalMessages[dayOfYear % motivationalMessages.size]

    // Screen-entry stagger
    var scoreVisible by remember { mutableStateOf(false) }
    var statsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        scoreVisible = true
        delay(150)
        statsVisible = true
    }

    val scoreEnter = if (reducedMotion) EnterTransition.None
    else fadeIn(tween(300)) + slideInVertically(
        initialOffsetY = { it / 4 },
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
    )
    val statsEnter = if (reducedMotion) EnterTransition.None
    else fadeIn(tween(300)) + slideInVertically(
        initialOffsetY = { it / 4 },
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            DashboardHeader()
        }
        item {
            AnimatedVisibility(visible = scoreVisible, enter = scoreEnter) {
                ScoreCard(user = user, partner = partner, reducedMotion = reducedMotion)
            }
        }
        item {
            AnimatedVisibility(visible = statsVisible, enter = statsEnter) {
                StatsRow(user = user, reducedMotion = reducedMotion)
            }
        }
        item {
            Text(
                text = stringResource(R.string.dashboard_recent_activity),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (recentEvents.isEmpty()) {
            item {
                EmptyActivityState()
            }
        } else {
            items(recentEvents, key = { it.id }) { event ->
                ActivityItem(
                    event = event,
                    modifier = Modifier.animateItem(
                        fadeInSpec = if (reducedMotion) null else tween(200),
                        placementSpec = if (reducedMotion) null else spring(stiffness = Spring.StiffnessMediumLow),
                    ),
                )
            }
        }
        item {
            androidx.compose.material3.OutlinedButton(
                onClick = onNavigateToReport,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.dashboard_view_report))
            }
        }
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = todayMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DashboardHeader() {
    val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    Column {
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = dateFormat.format(Date()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ScoreCard(user: User?, partner: Partner?, reducedMotion: Boolean) {
    val reducedMotionForCard = reducedMotion
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(reducedMotionForCard),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerScore(
                emoji = user?.emojiAvatar ?: "😊",
                name = user?.name ?: "You",
                points = user?.weeklyPoints ?: 200,
                isWinning = (user?.weeklyPoints ?: 200) > (partner?.weeklyPoints ?: 200),
                reducedMotion = reducedMotion,
            )

            Text(
                text = stringResource(R.string.dashboard_vs),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            PlayerScore(
                emoji = partner?.emojiAvatar ?: "❓",
                name = partner?.name ?: "Partner",
                points = partner?.weeklyPoints ?: 200,
                isWinning = (partner?.weeklyPoints ?: 200) > (user?.weeklyPoints ?: 200),
                reducedMotion = reducedMotion,
            )
        }
    }
}

@Composable
private fun PlayerScore(
    emoji: String,
    name: String,
    points: Int,
    isWinning: Boolean,
    reducedMotion: Boolean,
) {
    // Animated score count-up
    val animatedPoints by animateIntAsState(
        targetValue = points,
        animationSpec = if (reducedMotion) snap() else tween(800, easing = FastOutSlowInEasing),
        label = "scoreCount",
    )

    // Winning pulse on trophy icon alpha
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
        Text(text = emoji, fontSize = 40.sp)
        Spacer(modifier = Modifier.height(4.dp))
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
        Text(
            text = stringResource(R.string.dashboard_points),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun StatsRow(user: User?, reducedMotion: Boolean) {
    val animatedStreak by animateIntAsState(
        targetValue = user?.currentStreak ?: 0,
        animationSpec = if (reducedMotion) snap() else tween(800, easing = FastOutSlowInEasing),
        label = "streakCount",
    )
    val animatedResists by animateIntAsState(
        targetValue = user?.totalResists ?: 0,
        animationSpec = if (reducedMotion) snap() else tween(800, easing = FastOutSlowInEasing),
        label = "resistCount",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatItem(
            icon = Icons.Filled.Whatshot,
            iconDescription = stringResource(R.string.cd_streak),
            value = "$animatedStreak",
            label = stringResource(R.string.dashboard_streak),
            modifier = Modifier.weight(1f).pressScale(reducedMotion),
        )
        StatItem(
            icon = Icons.Filled.FitnessCenter,
            iconDescription = stringResource(R.string.cd_resists),
            value = "$animatedResists",
            label = stringResource(R.string.dashboard_resists_today),
            modifier = Modifier.weight(1f).pressScale(reducedMotion),
        )
        StatItem(
            icon = Icons.Filled.Timer,
            iconDescription = stringResource(R.string.cd_time_saved),
            value = "0",
            label = stringResource(R.string.dashboard_time_saved),
            modifier = Modifier.weight(1f).pressScale(reducedMotion),
        )
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    iconDescription: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EmptyActivityState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "🌱", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.dashboard_no_activity_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.dashboard_no_activity_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ActivityItem(event: PointEvent, modifier: Modifier = Modifier) {
    val positiveColor = pointsPositiveColor()
    val negativeColor = pointsNegativeColor()

    // Points delta bounce-in scale
    var targetScale by remember(event.id) { mutableStateOf(0f) }
    LaunchedEffect(event.id) { targetScale = 1f }
    val deltaScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "deltaScale",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (event.points >= 0) Icons.Filled.CheckCircle else Icons.Filled.PhoneAndroid,
            contentDescription = if (event.points >= 0) {
                stringResource(R.string.cd_activity_resisted)
            } else {
                stringResource(R.string.cd_activity_scrolled)
            },
            tint = if (event.points >= 0) positiveColor else negativeColor,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.reason,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(event.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = if (event.points >= 0) "+${event.points}" else "${event.points}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (event.points >= 0) positiveColor else negativeColor,
            modifier = Modifier.scale(deltaScale),
        )
    }
}
