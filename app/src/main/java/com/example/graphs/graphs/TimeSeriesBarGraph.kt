package com.example.graphs.graphs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graphs.models.GraphData
import com.example.graphs.models.TimePeriod


@Composable
fun TimeSeriesBarGraph(
    modifier: Modifier = Modifier,
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    dailyData: List<GraphData>,
    weeklyData: List<GraphData>,
    monthlyData: List<GraphData>,
    yearlyData: List<GraphData>
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Time period selection buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .horizontalScroll(rememberScrollState()),

            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeToggleButton("Daily", selectedPeriod is TimePeriod.Daily) {
                onPeriodSelected(TimePeriod.Daily)
            }
            TimeToggleButton("Weekly", selectedPeriod is TimePeriod.Weekly) {
                onPeriodSelected(TimePeriod.Weekly)
            }
            TimeToggleButton("Monthly", selectedPeriod is TimePeriod.Monthly) {
                onPeriodSelected(TimePeriod.Monthly)
            }
            TimeToggleButton("Yearly", selectedPeriod is TimePeriod.Yearly) {
                onPeriodSelected(TimePeriod.Yearly)
            }

            Spacer(modifier = Modifier.width(8.dp)) // End padding
        }


        // Graph card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val currentData = when (selectedPeriod) {
                    is TimePeriod.Daily -> dailyData
                    is TimePeriod.Weekly -> weeklyData
                    is TimePeriod.Monthly -> monthlyData
                    is TimePeriod.Yearly -> yearlyData
                }

                BarGraph(data = currentData)
            }
        }
    }
}

@Composable
private fun TimeToggleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF6750A4) else Color.LightGray,
            contentColor = if (selected) Color.White else Color.Black
        ),
        modifier = Modifier.height(36.dp), // Reduced height
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp) // Reduced padding
    ) {
        Text(
            text = text,
            fontSize = 14.sp // Smaller text
        )
    }
}

@Composable
private fun BarGraph(
    data: List<GraphData>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val barWidth = size.width / data.size
        val maxValue = data.maxOf { it.value }
        val heightRatio = size.height / maxValue

        // Draw grid lines
        val gridLines = 5
        val gridSpacing = size.height / gridLines
        repeat(gridLines + 1) { i ->
            val y = size.height - (i * gridSpacing)
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )

            // Draw value labels
            val value = (maxValue * i / gridLines)
            drawContext.canvas.nativeCanvas.drawText(
                String.format("%.1f", value),
                0f,
                y - 5,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.LEFT
                }
            )
        }

        // Draw bars
        data.forEachIndexed { index, item ->
            val barHeight = (item.value / maxValue) * size.height
            drawRect(
                color = Color(0xFF6750A4),
                topLeft = Offset(
                    x = index * barWidth + barWidth * 0.2f,
                    y = size.height - barHeight
                ),
                size = Size(
                    width = barWidth * 0.6f,
                    height = barHeight
                )
            )

            // Draw x-axis labels
            drawContext.canvas.nativeCanvas.drawText(
                item.label,
                (index * barWidth) + (barWidth / 2),
                size.height + 40,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}