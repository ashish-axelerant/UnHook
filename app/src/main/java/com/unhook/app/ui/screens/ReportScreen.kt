// UnHook — Weekly report screen with usage chart and stats
package com.unhook.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.unhook.app.ui.components.DayData
import com.unhook.app.ui.components.UsageChart
import com.unhook.app.ui.theme.pointsNegativeColor
import com.unhook.app.ui.theme.pointsPositiveColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    userRepository: UserRepository,
    pointsRepository: PointsRepository,
    onBack: () -> Unit,
) {
    val user by userRepository.getMe().collectAsState(initial = null)
    val partner by userRepository.getPartner().collectAsState(initial = null)
    val events by (user?.let { pointsRepository.getAllEvents(it.id) }
        ?: kotlinx.coroutines.flow.flowOf(emptyList())).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.report_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            WinnerBanner(user = user, partner = partner)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.report_weekly_chart),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            UsageChart(
                data = buildWeeklyData(events),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))

            WeeklyStatsCard(user = user, events = events)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WinnerBanner(user: User?, partner: Partner?) {
    val myPoints = user?.weeklyPoints ?: 200
    val partnerPoints = partner?.weeklyPoints ?: 200
    val isTied = myPoints == partnerPoints
    val iWon = myPoints > partnerPoints

    val positiveColor = pointsPositiveColor()
    val negativeColor = pointsNegativeColor()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isTied -> MaterialTheme.colorScheme.tertiaryContainer
                iWon -> positiveColor.copy(alpha = 0.15f)
                else -> negativeColor.copy(alpha = 0.15f)
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = when {
                    isTied -> "🤝"
                    iWon -> "🏆"
                    else -> "😅"
                },
                fontSize = 48.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    isTied -> stringResource(R.string.report_tied)
                    iWon -> stringResource(R.string.report_you_won)
                    else -> stringResource(R.string.report_you_lost, partner?.name ?: "Partner")
                },
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.report_score_summary, myPoints, partnerPoints),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeeklyStatsCard(user: User?, events: List<PointEvent>) {
    val resistCount = events.count { it.points > 0 }
    val scrollCount = events.count { it.points < 0 }
    val totalEarned = events.filter { it.points > 0 }.sumOf { it.points }
    val totalLost = events.filter { it.points < 0 }.sumOf { it.points }

    val positiveColor = pointsPositiveColor()
    val negativeColor = pointsNegativeColor()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.report_stats_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            ReportStatRow(stringResource(R.string.report_resists), "$resistCount", positiveColor)
            ReportStatRow(stringResource(R.string.report_scrolls), "$scrollCount", negativeColor)
            ReportStatRow(stringResource(R.string.report_points_earned), "+$totalEarned", positiveColor)
            ReportStatRow(stringResource(R.string.report_points_lost), "$totalLost", negativeColor)
            ReportStatRow(
                stringResource(R.string.report_current_streak),
                "${user?.currentStreak ?: 0} days",
                MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun ReportStatRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
) {
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
            color = valueColor,
        )
    }
}

private fun buildWeeklyData(events: List<PointEvent>): List<DayData> {
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val cal = Calendar.getInstance()

    return (6 downTo 0).map { daysAgo ->
        cal.timeInMillis = System.currentTimeMillis()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val dayStart = cal.apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }.timeInMillis
        val dayEnd = dayStart + 86_400_000L

        val dayEvents = events.filter { it.timestamp in dayStart until dayEnd }
        DayData(
            label = dayFormat.format(cal.time),
            earned = dayEvents.filter { it.points > 0 }.sumOf { it.points },
            lost = dayEvents.filter { it.points < 0 }.sumOf { it.points },
        )
    }
}
