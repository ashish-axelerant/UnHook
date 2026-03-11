// UnHook — 7-day usage bar chart drawn with Compose Canvas
package com.unhook.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    val maxValue = data.maxOfOrNull { maxOf(it.earned, kotlin.math.abs(it.lost)) }?.toFloat() ?: 1f

    Column(modifier = modifier) {
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
    }
}
