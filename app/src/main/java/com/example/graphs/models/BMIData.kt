package com.example.graphs.models

import java.util.Date

data class BMIData(
    val value: Double,
    val month: String,
    val date: Date = Date() // Added for more detailed tooltips
)

data class BMIPoint(
    val value: Double,
    val x: Float,
    val y: Float,
    val month: String,
    val date: Date
)