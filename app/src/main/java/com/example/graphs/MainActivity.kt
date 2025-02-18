package com.example.graphs

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.graphs.graphs.AnimatedGraph
import com.example.graphs.graphs.BloodPressureGraph
import com.example.graphs.graphs.InteractiveBMITracker
import com.example.graphs.graphs.SleepTrackerScreen
import com.example.graphs.graphs.TimeSeriesBarGraph
import com.example.graphs.graphs.WellnessScoreScreen
import com.example.graphs.models.BMIData
import com.example.graphs.models.GraphConfig
import com.example.graphs.models.GraphData
import com.example.graphs.models.GraphPoint
import com.example.graphs.models.LineConfig
import com.example.graphs.models.ReferenceLine
import com.example.graphs.models.ScoreCategory
import com.example.graphs.models.SleepData
import com.example.graphs.models.TimePeriod
import com.example.graphs.models.WellnessScore
import com.example.graphs.navigation.HomeScreen
import com.example.graphs.navigation.Screen
import com.example.graphs.navigation.ScreenWrapper
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthTrackingApp()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HealthTrackingApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.BloodPressure.route) {
            ScreenWrapper(title = Screen.BloodPressure.title, navController = navController) {
                HandleBloodPressureGraph()
            }
        }
        composable(Screen.LineChart.route) {
            ScreenWrapper(title = Screen.LineChart.title, navController = navController) {
                HandleLineChart()
            }
        }
        composable(Screen.BMI.route) {
            ScreenWrapper(title = Screen.BMI.title, navController = navController) {
                HandleBMIData()
            }
        }
        composable(Screen.Sleep.route) {
            ScreenWrapper(title = Screen.Sleep.title, navController = navController) {
                HandleSleepData()
            }
        }
        composable(Screen.Activity.route) {
            ScreenWrapper(title = Screen.Activity.title, navController = navController) {
                HandleBarGraph()
            }
        }
        composable(Screen.Wellness.route) {
            ScreenWrapper(title = Screen.Wellness.title, navController = navController) {
                HandleWellNessScore()
            }
        }
    }
}

@Composable
private fun HandleBloodPressureGraph() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5)
    ) {
        val systolicData = listOf(
            GraphPoint(120f, "May"),
            GraphPoint(122f, "Jun"),
            GraphPoint(118f, "Jul"),
            GraphPoint(125f, "Aug"),
            GraphPoint(130f, "Sep"),
            GraphPoint(128f, "Oct")
        )

        val diastolicData = listOf(
            GraphPoint(80f, "May"),
            GraphPoint(82f, "Jun"),
            GraphPoint(78f, "Jul"),
            GraphPoint(85f, "Aug"),
            GraphPoint(88f, "Sep"),
            GraphPoint(84f, "Oct")
        )

        BloodPressureGraph(
            systolicData = systolicData,
            diastolicData = diastolicData
        )
    }
}

@Composable
private fun HandleLineChart() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5)
    ) {
        val data = listOf(
            GraphPoint(120f, "May"),
            GraphPoint(122f, "Jun"),
            GraphPoint(118f, "Jul"),
            GraphPoint(125f, "Aug"),
            GraphPoint(130f, "Sep"),
            GraphPoint(128f, "Oct")
        )

        val config = GraphConfig(
            title = "Blood Pressure",
            referenceLines = listOf(
                ReferenceLine(120f, "Normal"),
                ReferenceLine(80f, "Low")
            ),
            yAxisRange = 60f..140f
        )

        val lineConfig = LineConfig(
            color = Color(0xFF6750A4),
            label = "Systolic"
        )

        AnimatedGraph(
            data = data,
            config = config,
            lineConfig = lineConfig,
            onPointSelected = { point ->
                // Handle point selection
            }
        )
    }
}

@Composable
private fun HandleBMIData() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5)
    ) {
        val sampleData = listOf(
            BMIData(26.8, "May"),
            BMIData(26.5, "Jun"),
            BMIData(26.2, "Jul"),
            BMIData(25.8, "Aug"),
            BMIData(25.5, "Sep"),
            BMIData(25.8, "Oct")
        )

        InteractiveBMITracker(
            bmiData = sampleData,
            normalRange = 25.0,
            goalBMI = 26.0
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HandleSleepData() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5)
    ) {
        // Sample data
        val sampleData = listOf(
            SleepData(7, LocalDate.of(2024, 8, 13)),
            SleepData(6, LocalDate.of(2024, 8, 14)),
            SleepData(7, LocalDate.of(2024, 8, 15)),
            SleepData(7, LocalDate.of(2024, 8, 16)),
            SleepData(8, LocalDate.of(2024, 8, 17)),
            SleepData(4, LocalDate.of(2024, 8, 18)),
            SleepData(7, LocalDate.of(2024, 8, 19))
        )

        SleepTrackerScreen(sleepData = sampleData)
    }
}

@Composable
private fun HandleBarGraph() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5)
    ) {
        var selectedPeriod by remember { mutableStateOf<TimePeriod>(TimePeriod.Daily) }

        val dailyData = listOf(
            GraphData(2.0f, "9a"),
            GraphData(2.5f, "10a"),
            GraphData(3.0f, "12p"),
            GraphData(4.5f, "3p"),
            GraphData(3.8f, "4p"),
            GraphData(3.2f, "6p"),
            GraphData(2.8f, "9p")
        )

        val weeklyData = listOf(
            GraphData(4.5f, "Sun"),
            GraphData(2.8f, "Mon"),
            GraphData(3.2f, "Tue"),
            GraphData(4.8f, "Wed"),
            GraphData(4.2f, "Thu"),
            GraphData(1.5f, "Fri"),
            GraphData(3.8f, "Sat")
        )

        val monthlyData = listOf(
            GraphData(4.2f, "Apr"),
            GraphData(3.8f, "May"),
            GraphData(2.5f, "Jun"),
            GraphData(4.0f, "Jul"),
            GraphData(3.5f, "Aug"),
            GraphData(4.2f, "Sep")
        )

        val yearlyData = listOf(
            GraphData(3.2f, "2021"),
            GraphData(3.8f, "2022"),
            GraphData(4.5f, "2023")
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Title
            Text(
                text = "Activity Trends",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            // Graph component
            TimeSeriesBarGraph(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it },
                dailyData = dailyData,
                weeklyData = weeklyData,
                monthlyData = monthlyData,
                yearlyData = yearlyData
            )
        }
    }
}

@Composable
private fun HandleWellNessScore() {
    val sampleData = WellnessScore(
        overallScore = 70,
        weeklyTrendPoints = 2,
        categories = listOf(
            ScoreCategory("Health", 82, Color(0xFF4CAF50)),     // Green
            ScoreCategory("Physical", 51, Color(0xFF6750A4)),   // Purple
            ScoreCategory("Emotional", 35, Color(0xFF2196F3))   // Blue
        ),
        lastUpdated = "Oct 23, 2024 at 2:52 PM"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5)
    ) {
        WellnessScoreScreen(
            wellnessData = sampleData,
            onRefresh = {
                // Handle refresh action
            }
        )
    }
}
