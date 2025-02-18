package com.example.graphs.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class GraphPoint(
    val value: Float,
    val label: String
)

data class GraphConfig(
    val title: String = "",
    val showLegend: Boolean = false,
    val referenceLines: List<ReferenceLine> = emptyList(),
    val yAxisRange: ClosedRange<Float> = 0f..100f,
    val gridLines: Int = 5,
    val animationDuration: Int = 1000
)

data class ReferenceLine(
    val value: Float,
    val label: String,
    val color: Color = Color.Gray,
    val isDashed: Boolean = true
)

data class LineConfig(
    val color: Color,
    val label: String,
    val strokeWidth: Float = 2f,
    val isDashed: Boolean = false
)

data class TooltipData(
    val point: GraphPoint,
    val position: Offset,
    val color: Color
)