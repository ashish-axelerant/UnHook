// UnHook — Dashboard with VS score card, stats row, and activity feed
package com.unhook.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
        Text(text = if (isWinning) "👑" else "", fontSize = 16.sp)
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
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatItem(emoji = "🔥", value = "${user?.currentStreak ?: 0}", label = stringResource(R.string.dashboard_streak))
        StatItem(emoji = "✊", value = "${user?.totalResists ?: 0}", label = stringResource(R.string.dashboard_resists_today))
        StatItem(emoji = "⏱️", value = "0", label = stringResource(R.string.dashboard_time_saved))
    }
}

@Composable
private fun StatItem(emoji: String, value: String, label: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ActivityItem(event: PointEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (event.points >= 0) "✊" else "📱",
            fontSize = 20.sp,
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
