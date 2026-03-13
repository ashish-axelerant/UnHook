// UnHook — 7-day usage bar chart drawn with Compose Canvas, with legend and empty state
package com.unhook.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unhook.app.R
import com.unhook.app.ui.theme.PointsGreen
import com.unhook.app.ui.theme.PointsRed

data class DayData(
    val label: String,
    val earned: Int,
    val lost: Int,
)

@Composable
fun UsageChart(
    data: List<DayData>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (data.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.chart_no_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            return@Column
        }

        val maxValue = data.maxOfOrNull { maxOf(it.earned, kotlin.math.abs(it.lost)) }?.toFloat() ?: 1f

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
        ) {
            val barWidth = size.width / (data.size * 3f)
            val spacing = barWidth * 0.5f

            data.forEachIndexed { index, day ->
                val x = index * (barWidth * 2 + spacing) + spacing

                // Earned bar (green)
                val earnedHeight = if (maxValue > 0) (day.earned / maxValue) * size.height * 0.85f else 0f
                drawRoundRect(
                    color = PointsGreen,
                    topLeft = Offset(x, size.height - earnedHeight),
                    size = Size(barWidth, earnedHeight),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                )

                // Lost bar (red)
                val lostHeight = if (maxValue > 0) (kotlin.math.abs(day.lost) / maxValue) * size.height * 0.85f else 0f
                drawRoundRect(
                    color = PointsRed,
                    topLeft = Offset(x + barWidth, size.height - lostHeight),
                    size = Size(barWidth, lostHeight),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                )
            }
        }

        // Day labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            data.forEach { day ->
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendDot(color = PointsGreen)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.chart_legend_earned),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(16.dp))
            LegendDot(color = PointsRed)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.chart_legend_lost),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color),
    )
}
