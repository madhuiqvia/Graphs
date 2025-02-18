package com.example.graphs.graphs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graphs.models.SleepData
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SleepTrackerScreen(
    sleepData: List<SleepData>,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Sleep Tracker",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Sleep circles row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Spacer(modifier = Modifier.width(4.dp)) // Start padding

                    sleepData.forEach { data ->
                        SleepDayIndicator(
                            hours = data.hours,
                            date = data.date,
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp)) // End padding
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Learn more section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Learn more",
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Learn how optimizing sleep can improve your overall health",
                        color = Color.DarkGray,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SleepDayIndicator(
    hours: Int,
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(72.dp)
    ) {
        // Circular progress
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(72.dp)
        ) {
            Canvas(modifier = Modifier.size(72.dp)) {
                // Background circle
                drawArc(
                    color = Color(0xFFF5F5F5),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )

                // Progress arc
                val progress = minOf(1f, hours / 8f) // Cap at 100% when hours >= 8
                drawArc(
                    color = Color(0xFF6750A4),
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )
            }

            Text(
                text = "${hours}h",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day of week
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEE")),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        // Date
        Text(
            text = date.format(DateTimeFormatter.ofPattern("MM/dd")),
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}