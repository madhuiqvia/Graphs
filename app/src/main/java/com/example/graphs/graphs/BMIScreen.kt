package com.example.graphs.graphs

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graphs.models.BMIData
import com.example.graphs.models.BMIPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

@Composable
fun InteractiveBMITracker(
    modifier: Modifier = Modifier,
    bmiData: List<BMIData>,
    normalRange: Double = 25.0,
    goalBMI: Double = 26.0,
    onPointSelected: (BMIData) -> Unit = {}
) {
    var selectedPoint by remember { mutableStateOf<BMIPoint?>(null) }
    val animationProgress = remember { Animatable(0f) }
    val interactionScale = remember { Animatable(1f) }

    LaunchedEffect(bmiData) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1500, easing = FastOutSlowInEasing)
        )
    }
// write white card color
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardColors(contentColor = Color.Black, containerColor = Color.White, disabledContentColor = Color.White, disabledContainerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with animation
            Text(
                text = "BMI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (selectedPoint != null) {
                Text(
                    text = "BMI: ${String.format("%.1f", selectedPoint?.value)} on ${
                        SimpleDateFormat(
                            "MMM dd, yyyy",
                            Locale.getDefault()
                        ).format(selectedPoint?.date)
                    }",
                    color = Color(0xFF6750A4),
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "Your BMI trend over time",
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Interactive Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                InteractiveBMIGraph(
                    bmiData = bmiData,
                    normalRange = normalRange,
                    goalBMI = goalBMI,
                    animationProgress = animationProgress.value,
                    selectedPoint = selectedPoint,
                    onPointSelected = { point ->
                        selectedPoint = point
                        onPointSelected(BMIData(point.value, point.month, point.date))
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats
            AnimatedBMIStats(bmiData = bmiData)
        }
    }
}


// Add this function inside the Canvas scope
private fun DrawScope.drawGridAndReferences(
    padding: Float,
    width: Float,
    height: Float,
    minBMI: Double,
    maxBMI: Double,
    normalRange: Double,
    goalBMI: Double,
    bmiRange: Double,
    effectiveHeight: Float
) {
    // Draw horizontal grid lines and BMI values
    val gridLines = 5
    val bmiStep = bmiRange / gridLines
    repeat(gridLines + 1) { i ->
        val y = padding + (effectiveHeight * (1 - i.toFloat() / gridLines))
        val bmiValue = minBMI + (bmiStep * i)

        // Grid line
        drawLine(
            color = Color.LightGray,
            start = Offset(padding, y),
            end = Offset(width - padding, y),
            strokeWidth = 1f
        )

        // BMI value
        drawContext.canvas.nativeCanvas.drawText(
            round(bmiValue).toInt().toString(),
            padding - 30f,
            y + 10f,
            Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 30f
                textAlign = android.graphics.Paint.Align.RIGHT
            }
        )
    }

    // Draw normal range line (dotted)
    val normalRangeY = padding + effectiveHeight * (1 - (normalRange - minBMI) / bmiRange)
    drawLine(
        color = Color.Gray,
        start = Offset(padding, normalRangeY.toFloat()),
        end = Offset(width - padding, normalRangeY.toFloat()),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
        strokeWidth = 2f
    )

    // Draw goal line (dashed)
    val goalY = padding + effectiveHeight * (1 - (goalBMI - minBMI) / bmiRange)
    drawLine(
        color = Color.Gray,
        start = Offset(padding, goalY.toFloat()),
        end = Offset(width - padding, goalY.toFloat()),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f),
        strokeWidth = 2f
    )
}

@Composable
private fun InteractiveBMIGraph(
    bmiData: List<BMIData>,
    normalRange: Double,
    goalBMI: Double,
    animationProgress: Float,
    selectedPoint: BMIPoint?,
    onPointSelected: (BMIPoint) -> Unit
) {
    val scope = rememberCoroutineScope()
    var points by remember { mutableStateOf(emptyList<BMIPoint>()) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val nearestPoint = points.minByOrNull { point ->
                        abs(point.x - offset.x) + abs(point.y - offset.y)
                    }
                    nearestPoint?.let { point ->
                        if (abs(point.x - offset.x) < 30f && abs(point.y - offset.y) < 30f) {
                            onPointSelected(point)
                            scope.launch {
                                // Add tap animation here if needed
                            }
                        }
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val padding = 40f

        val effectiveWidth = width - (padding * 2)
        val effectiveHeight = height - (padding * 2)

        val minBMI = (bmiData.minOf { it.value } - 1).coerceAtLeast(20.0)
        val maxBMI = (bmiData.maxOf { it.value } + 1).coerceAtMost(35.0)
        val bmiRange = maxBMI - minBMI

        // Update points list for interaction
        points = bmiData.mapIndexed { index, data ->
            val x = padding + (index.toFloat() * effectiveWidth / (bmiData.size - 1))
            val targetY = padding + effectiveHeight * (1 - (data.value - minBMI) / bmiRange)
            val startY = padding + effectiveHeight
            val currentY = lerp(startY, targetY.toFloat(), animationProgress)
            BMIPoint(data.value, x, currentY, data.month, data.date)
        }

        // Draw grid and reference lines
        drawGridAndReferences(
            padding = padding,
            width = width,
            height = height,
            minBMI = minBMI,
            maxBMI = maxBMI,
            normalRange = normalRange,
            goalBMI = goalBMI,
            bmiRange = bmiRange,
            effectiveHeight = effectiveHeight
        )

        // Draw BMI line
        val path = Path()
        points.forEachIndexed { index, point ->
            if (index == 0) {
                path.moveTo(point.x, point.y)
            } else {
                val prevPoint = points[index - 1]
                val controlPoint1X = prevPoint.x + (point.x - prevPoint.x) * 0.5f
                val controlPoint1Y = prevPoint.y
                val controlPoint2X = point.x - (point.x - prevPoint.x) * 0.5f
                val controlPoint2Y = point.y

                path.cubicTo(
                    controlPoint1X, controlPoint1Y,
                    controlPoint2X, controlPoint2Y,
                    point.x, point.y
                )
            }
        }

        // Draw animated path
        drawPath(
            path = path,
            color = Color(0xFF6750A4),
            style = Stroke(
                width = 3f,
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(30f, 10f),
                    phase = -animationProgress * 40f
                )
            )
        )

        // Draw points and highlight selected point
        points.forEach { point ->
            val isSelected = selectedPoint?.x == point.x && selectedPoint.y == point.y
            val pointRadius = if (isSelected) 8f else 5f
            val pointColor = if (isSelected) Color(0xFF6750A4) else Color(0xFF9E9E9E)

            drawCircle(
                color = Color.White,
                radius = pointRadius + 2f,
                center = Offset(point.x, point.y)
            )
            drawCircle(
                color = pointColor,
                radius = pointRadius,
                center = Offset(point.x, point.y)
            )
        }

        // Draw month labels
        points.forEach { point ->
            drawContext.canvas.nativeCanvas.drawText(
                point.month,
                point.x,
                size.height - 10f,
                Paint().apply {
                    color = android.graphics.Color.GRAY
                    textAlign = Paint.Align.CENTER
                    textSize = 30f
                }
            )
        }
    }
}

private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction.coerceIn(0f, 1f)
}

@Composable
private fun AnimatedBMIStats(bmiData: List<BMIData>) {
    val minValue = bmiData.minOf { it.value }
    val maxValue = bmiData.maxOf { it.value }
    val difference = maxValue - minValue

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AnimatedStatValue(
            value = minValue,
            label = "Min. value",
            color = Color.Red
        )
        AnimatedStatValue(
            value = maxValue,
            label = "Max. value"
        )
        AnimatedStatValue(
            value = difference,
            label = "Difference"
        )
    }
}

@Composable
private fun AnimatedStatValue(
    value: Double,
    label: String,
    color: Color = Color(0xFF6750A4)
) {
    var previousValue by remember { mutableStateOf(value) }
    var displayValue by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        animate(
            initialValue = previousValue.toFloat(),
            targetValue = value.toFloat(),
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        ) { currentValue, _ ->
            displayValue = currentValue.toDouble()
        }
        previousValue = value
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = String.format("%.1f", displayValue),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}