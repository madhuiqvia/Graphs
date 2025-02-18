package com.example.graphs.models


data class GraphData(
    val value: Float,
    val label: String
)

sealed class TimePeriod {
    data object Daily : TimePeriod()
    data object Weekly : TimePeriod()
    data object Monthly : TimePeriod()
    data object Yearly : TimePeriod()
}