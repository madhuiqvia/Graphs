package com.example.graphs.graphs

import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.graphs.models.GraphConfig
import com.example.graphs.models.GraphPoint
import com.example.graphs.models.LineConfig
import com.example.graphs.models.TooltipData
import kotlin.math.abs
import kotlin.math.roundToInt

// Main graph component
@Composable
fun AnimatedGraph(
    modifier: Modifier = Modifier,
    data: List<GraphPoint>,
    config: GraphConfig,
    lineConfig: LineConfig,
    onPointSelected: (GraphPoint?) -> Unit = {}
) {
    var tooltipData by remember { mutableStateOf<TooltipData?>(null) }
    var expandedInfo by remember { mutableStateOf(false) }

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = config.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { expandedInfo = !expandedInfo }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = lineConfig.color
                    )
                }
            }

            // Info section
            AnimatedVisibility(
                visible = expandedInfo,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    config.referenceLines.forEach { reference ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Canvas(modifier = Modifier.size(24.dp)) {
                                drawLine(
                                    color = reference.color,
                                    start = Offset(0f, size.height / 2),
                                    end = Offset(size.width, size.height / 2),
                                    strokeWidth = 2f,
                                    pathEffect = if (reference.isDashed)
                                        PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    else null
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${reference.label}: ${reference.value}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Graph content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(top = 16.dp)
            ) {
                GraphContent(
                    data = data,
                    config = config,
                    lineConfig = lineConfig,
                    onTooltipChanged = { tooltipData = it },
                    onPointSelected = onPointSelected
                )

                // Tooltip
                tooltipData?.let { tooltip ->
                    TooltipCard(tooltipData = tooltip)
                }
            }
        }
    }
}

@Composable
private fun GraphContent(
    data: List<GraphPoint>,
    config: GraphConfig,
    lineConfig: LineConfig,
    onTooltipChanged: (TooltipData?) -> Unit,
    onPointSelected: (GraphPoint?) -> Unit
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = config.animationDuration,
                easing = FastOutSlowInEasing
            )
        )
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val point = findNearestPoint(
                        offset,
                        data,
                        size,
                        config.yAxisRange
                    )
                    if (point != null) {
                        onTooltipChanged(
                            TooltipData(
                                point.first,
                                Offset(point.second.x, point.second.y),
                                lineConfig.color
                            )
                        )
                        onPointSelected(point.first)
                    } else {
                        onTooltipChanged(null)
                        onPointSelected(null)
                    }
                }
            }
    ) {
        drawGraph(
            data = data,
            config = config,
            lineConfig = lineConfig,
            progress = animationProgress.value
        )
    }
}

fun findNearestPoint(
    offset: Offset,
    data: List<GraphPoint>,
    size: IntSize,
    yAxisRange: ClosedRange<Float>
): Pair<GraphPoint, Offset>? {
    val padding = 40f
    val effectiveWidth = size.width - (padding * 2)
    val effectiveHeight = size.height - (padding * 2)
    val yRange = yAxisRange.endInclusive - yAxisRange.start

    return data.mapIndexed { index, point ->
        val x = padding + (index.toFloat() * effectiveWidth / (data.size - 1))
        val y = padding + effectiveHeight * (1 - (point.value - yAxisRange.start) / yRange)
        val distance = abs(offset.x - x) + abs(offset.y - y)
        Triple(point, Offset(x, y), distance)
    }.minByOrNull { it.third }?.let {
        if (it.third < 50f) Pair(it.first, it.second) else null
    }
}

private fun DrawScope.drawGraph(
    data: List<GraphPoint>,
    config: GraphConfig,
    lineConfig: LineConfig,
    progress: Float
) {
    val padding = 40f
    val effectiveWidth = size.width - (padding * 2)
    val effectiveHeight = size.height - (padding * 2)
    val yRange = config.yAxisRange.endInclusive - config.yAxisRange.start

    // Draw grid and reference lines
    drawBackground(config, padding, effectiveWidth, effectiveHeight)

    // Draw animated line
    val path = Path()
    val points = data.mapIndexed { index, point ->
        val x = padding + (index.toFloat() * effectiveWidth / (data.size - 1))
        val targetY =
            padding + effectiveHeight * (1 - (point.value - config.yAxisRange.start) / yRange)
        val currentY = lerp(size.height, targetY, progress)
        Offset(x, currentY)
    }

    // Draw curved line
    points.forEachIndexed { index, point ->
        if (index == 0) {
            path.moveTo(point.x, point.y)
        } else {
            val prevPoint = points[index - 1]
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

    // Draw line with animation
    drawPath(
        path = path,
        color = lineConfig.color,
        style = Stroke(
            width = lineConfig.strokeWidth,
            pathEffect = if (lineConfig.isDashed)
                PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            else null
        ),
        alpha = progress
    )

    // Draw points
    points.forEach { point ->
        drawCircle(
            color = Color.White,
            radius = 6f,
            center = point,
            alpha = progress
        )
        drawCircle(
            color = lineConfig.color,
            radius = 4f,
            center = point,
            alpha = progress
        )
    }

    // Draw labels
    data.forEachIndexed { index, point ->
        val x = padding + (index.toFloat() * effectiveWidth / (data.size - 1))
        drawContext.canvas.nativeCanvas.drawText(
            point.label,
            x,
            size.height - 10f,
            Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 30f
                textAlign = Paint.Align.CENTER
                alpha = (255 * progress).toInt()
            }
        )
    }
}

@Composable
private fun TooltipCard(tooltipData: TooltipData) {
    Card(
        modifier = Modifier
            .offset(
                x = with(LocalDensity.current) { tooltipData.position.x.toDp() - 50.dp },
                y = with(LocalDensity.current) { tooltipData.position.y.toDp() - 70.dp }
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%.1f", tooltipData.point.value),
                style = MaterialTheme.typography.bodyLarge,
                color = tooltipData.color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = tooltipData.point.label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

fun DrawScope.drawBackground(
    config: GraphConfig,
    padding: Float,
    effectiveWidth: Float,
    effectiveHeight: Float
) {
    val yRange = config.yAxisRange.endInclusive - config.yAxisRange.start

    // Draw grid lines and labels
    repeat(config.gridLines + 1) { i ->
        val y = padding + (effectiveHeight * (1 - i.toFloat() / config.gridLines))
        val value = config.yAxisRange.start + (yRange * i.toFloat() / config.gridLines)

        drawLine(
            color = Color.LightGray,
            start = Offset(padding, y),
            end = Offset(padding + effectiveWidth, y),
            strokeWidth = 1f
        )

        drawContext.canvas.nativeCanvas.drawText(
            value.roundToInt().toString(),
            padding - 30f,
            y + 10f,
            Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 30f
                textAlign = Paint.Align.RIGHT
            }
        )
    }

    // Draw reference lines
    config.referenceLines.forEach { reference ->
        val y =
            padding + effectiveHeight * (1 - (reference.value - config.yAxisRange.start) / yRange)
        drawLine(
            color = reference.color,
            start = Offset(padding, y),
            end = Offset(padding + effectiveWidth, y),
            pathEffect = if (reference.isDashed)
                PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            else null,
            strokeWidth = 1f
        )
    }
}

// Helper function
private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction.coerceIn(0f, 1f)
}