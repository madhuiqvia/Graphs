package com.example.graphs.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Health Dashboard")
    data object BloodPressure : Screen("blood_pressure", "Blood Pressure")
    data object LineChart : Screen("line_chart", "Blood Pressure Chart")
    data object BMI : Screen("bmi", "BMI Tracker")
    data object Sleep : Screen("sleep", "Sleep Tracker")
    data object Activity : Screen("activity", "Activity Trends")
    data object Wellness : Screen("wellness", "Wellness Score")
}