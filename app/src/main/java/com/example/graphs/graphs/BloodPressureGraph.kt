package com.example.graphs.graphs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.graphs.models.GraphConfig
import com.example.graphs.models.GraphPoint
import com.example.graphs.models.LineConfig
import com.example.graphs.models.ReferenceLine
import com.example.graphs.models.TooltipData
import kotlin.math.abs

@Composable
fun BloodPressureGraph(
    systolicData: List<GraphPoint>,
    diastolicData: List<GraphPoint>,
    modifier: Modifier = Modifier
) {
    var selectedPoint by remember { mutableStateOf<Pair<GraphPoint, GraphPoint>?>(null) }

    val config = GraphConfig(
        title = "Blood Pressure",
        referenceLines = listOf(
            ReferenceLine(120f, "Normal Systolic", Color(0xFF6750A4)),
            ReferenceLine(80f, "Normal Diastolic", Color(0xFF9C27B0))
        ),
        yAxisRange = 60f..160f,
        gridLines = 5
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with legend
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Blood Pressure",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Legend
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendItem(
                        color = Color(0xFF6750A4),
                        text = "Systolic"
                    )
                    LegendItem(
                        color = Color(0xFF9C27B0),
                        text = "Diastolic"
                    )
                }

                // Selected point info
                selectedPoint?.let { (systolic, diastolic) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "${systolic.label}: ${systolic.value.toInt()}/${diastolic.value.toInt()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // Background and grid
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawBackground(config, 40f, size.width - 80f, size.height - 80f)
                }

                // Systolic line
                AnimatedLineGraph(
                    points = systolicData,
                    config = config,
                    lineConfig = LineConfig(
                        color = Color(0xFF6750A4),
                        label = "Systolic",
                        strokeWidth = 3f
                    ),
                    onPointSelected = { point ->
                        selectedPoint = point?.let {
                            val diastolicPoint = diastolicData.find { it.label == point.label }
                            if (diastolicPoint != null) Pair(point, diastolicPoint) else null
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Diastolic line
                AnimatedLineGraph(
                    points = diastolicData,
                    config = config,
                    lineConfig = LineConfig(
                        color = Color(0xFF9C27B0),
                        label = "Diastolic",
                        strokeWidth = 3f
                    ),
                    onPointSelected = { point ->
                        selectedPoint = point?.let {
                            val systolicPoint = systolicData.find { it.label == point.label }
                            if (systolicPoint != null) Pair(systolicPoint, point) else null
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun AnimatedLineGraph(
    points: List<GraphPoint>,
    config: GraphConfig,
    lineConfig: LineConfig,
    modifier: Modifier = Modifier,
    onPointSelected: (GraphPoint?) -> Unit = {}
) {
    var tooltipData by remember { mutableStateOf<TooltipData?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1500, easing = FastOutSlowInEasing)
        )
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val nearestPoint = findNearestPoint(offset, points, size, config.yAxisRange)
                    if (nearestPoint != null) {
                        tooltipData = TooltipData(
                            nearestPoint.first,
                            nearestPoint.second,
                            lineConfig.color
                        )
                        onPointSelected(nearestPoint.first)
                    } else {
                        tooltipData = null
                        onPointSelected(null)
                    }
                }
            }
    ) {
        val padding = 40f
        val effectiveWidth = size.width - (padding * 2)
        val effectiveHeight = size.height - (padding * 2)
        val yRange = config.yAxisRange.endInclusive - config.yAxisRange.start

        // Draw animated line
        val path = Path()
        val animatedPoints = points.mapIndexed { index, point ->
            val x = padding + (index.toFloat() * effectiveWidth / (points.size - 1))
            val targetY =
                padding + effectiveHeight * (1 - (point.value - config.yAxisRange.start) / yRange)
            val startY = size.height
            val currentY = lerp(startY, targetY, animationProgress.value)
            Offset(x, currentY)
        }

        // Draw curved line
        animatedPoints.forEachIndexed { index, point ->
            if (index == 0) {
                path.moveTo(point.x, point.y)
            } else {
                val prevPoint = animatedPoints[index - 1]
                val controlPoint1 = Offset(
                    prevPoint.x + (point.x - prevPoint.x) * 0.5f,
                    prevPoint.y
                )
                val controlPoint2 = Offset(
                    point.x - (point.x - prevPoint.x) * 0.5f,
                    point.y
                )
                path.cubicTo(
                    controlPoint1.x, controlPoint1.y,
                    controlPoint2.x, controlPoint2.y,
                    point.x, point.y
                )
            }
        }

        // Draw the path with animation
        drawPath(
            path = path,
            color = lineConfig.color,
            style = Stroke(
                width = lineConfig.strokeWidth,
                pathEffect = if (lineConfig.isDashed)
                    PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                else null
            ),
            alpha = animationProgress.value
        )

        // Draw points
        animatedPoints.forEachIndexed { index, point ->
            val isSelected = tooltipData?.point == points[index]
            val pointRadius = if (isSelected) 6f else 4f

            // White background circle
            drawCircle(
                color = Color.White,
                radius = pointRadius + 2f,
                center = point,
                alpha = animationProgress.value
            )
            // Colored point
            drawCircle(
                color = lineConfig.color,
                radius = pointRadius,
                center = point,
                alpha = animationProgress.value
            )
        }
    }
}

private fun findNearestPoint(
    offset: Offset,
    points: List<GraphPoint>,
    size: Size,
    yAxisRange: ClosedRange<Float>
): Pair<GraphPoint, Offset>? {
    val padding = 40f
    val effectiveWidth = size.width - (padding * 2)
    val effectiveHeight = size.height - (padding * 2)
    val yRange = yAxisRange.endInclusive - yAxisRange.start

    return points.mapIndexed { index, point ->
        val x = padding + (index.toFloat() * effectiveWidth / (points.size - 1))
        val y = padding + effectiveHeight * (1 - (point.value - yAxisRange.start) / yRange)
        val distance = abs(offset.x - x) + abs(offset.y - y)
        Triple(point, Offset(x, y), distance)
    }.minByOrNull { it.third }?.let {
        if (it.third < 50f) Pair(it.first, it.second) else null
    }
}

private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction.coerceIn(0f, 1f)
}