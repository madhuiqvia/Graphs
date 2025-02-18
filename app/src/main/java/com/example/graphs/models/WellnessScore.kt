package com.example.graphs.models

import androidx.compose.ui.graphics.Color

data class WellnessScore(
    val overallScore: Int,
    val weeklyTrendPoints: Int,
    val categories: List<ScoreCategory>,
    val lastUpdated: String
)

data class ScoreCategory(
    val label: String,
    val score: Int,
    val color: Color
)