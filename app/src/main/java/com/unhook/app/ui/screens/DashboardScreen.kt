// UnHook — Dashboard with VS score card, stats row, and activity feed
package com.unhook.app.ui.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.unhook.app.ui.theme.PointsGreen
import com.unhook.app.ui.theme.PointsRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
    val todayMessage = motivationalMessages[dayOfYear % motivationalMessages.size]

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
            ScoreCard(user = user, partner = partner)
        }
        item {
            StatsRow(user = user)
        }
        item {
            Text(
                text = stringResource(R.string.dashboard_recent_activity),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (recentEvents.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.dashboard_no_activity),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(recentEvents) { event ->
                ActivityItem(event = event)
            }
        }
        item {
            androidx.compose.material3.OutlinedButton(
                onClick = onNavigateToReport,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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
private fun ScoreCard(user: User?, partner: Partner?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Your side
            PlayerScore(
                emoji = user?.emojiAvatar ?: "😊",
                name = user?.name ?: "You",
                points = user?.weeklyPoints ?: 200,
                isWinning = (user?.weeklyPoints ?: 200) > (partner?.weeklyPoints ?: 200),
            )

            // VS
            Text(
                text = stringResource(R.string.dashboard_vs),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            // Partner side
            PlayerScore(
                emoji = partner?.emojiAvatar ?: "❓",
                name = partner?.name ?: "Partner",
                points = partner?.weeklyPoints ?: 200,
                isWinning = (partner?.weeklyPoints ?: 200) > (user?.weeklyPoints ?: 200),
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
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Fixed-height Box prevents layout shift when winning state changes
        Box(modifier = Modifier.height(22.dp), contentAlignment = Alignment.Center) {
            if (isWinning) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = stringResource(R.string.cd_winning),
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp),
                )
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
            text = "$points",
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
private fun StatsRow(user: User?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatItem(
            icon = Icons.Filled.Whatshot,
            iconDescription = stringResource(R.string.cd_streak),
            value = "${user?.currentStreak ?: 0}",
            label = stringResource(R.string.dashboard_streak),
            modifier = Modifier.weight(1f),
        )
        StatItem(
            icon = Icons.Filled.FitnessCenter,
            iconDescription = stringResource(R.string.cd_resists),
            value = "${user?.totalResists ?: 0}",
            label = stringResource(R.string.dashboard_resists_today),
            modifier = Modifier.weight(1f),
        )
        StatItem(
            icon = Icons.Filled.Timer,
            iconDescription = stringResource(R.string.cd_time_saved),
            value = "0",
            label = stringResource(R.string.dashboard_time_saved),
            modifier = Modifier.weight(1f),
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
private fun ActivityItem(event: PointEvent) {
    Row(
        modifier = Modifier
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
            tint = if (event.points >= 0) PointsGreen else PointsRed,
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
            color = if (event.points >= 0) PointsGreen else PointsRed,
        )
    }
}
