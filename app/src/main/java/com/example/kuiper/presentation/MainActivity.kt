package com.example.kuiper.presentation

import com.example.kuiper.presentation.worker.ReminderWorker
import com.example.kuiper.presentation.sensor.StepSensorManager
import com.example.kuiper.presentation.data.StepDatabase
import com.example.kuiper.presentation.data.StepPrefs
import com.example.kuiper.presentation.data.DailyStepEntity
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.TimeUnit

data class AnimalTier(val minSteps: Int, val name: String, val emoji: String)

val animalList = listOf(
    AnimalTier(0, "Caracol", "🐌"),
    AnimalTier(1000, "Tortuga", "🐢"),
    AnimalTier(2000, "Perrito de la pradera", "🐿️"),
    AnimalTier(3000, "Conejito", "🐇"),
    AnimalTier(4000, "Zorro", "🦊"),
    AnimalTier(5000, "Venado", "🦌"),
    AnimalTier(6000, "Lobo", "🐺"),
    AnimalTier(7000, "Caballo", "🐎"),
    AnimalTier(8000, "Antílope", "🦌"),
    AnimalTier(9000, "Galgo", "🐕"),
    AnimalTier(10000, "Chita", "🐆")
)

fun getAnimalForSteps(steps: Int): AnimalTier {
    return animalList.lastOrNull { steps >= it.minSteps } ?: animalList.first()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(2, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "StepReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        setContent {
            MaterialTheme {
                StepTrackerApp()
            }
        }
    }
}

@Composable
fun StepTrackerApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { StepPrefs(context) }
    val db = remember { StepDatabase.getDatabase(context) }
    val sensorManager = remember { StepSensorManager(context) }

    val savedGoal by prefs.dailyGoal.collectAsState(initial = null)
    val weeklyData by db.stepDao().getLast7Days().collectAsState(initial = emptyList())
    val liveSteps by sensorManager.observeSteps().collectAsState(initial = 0)

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }

    LaunchedEffect(liveSteps, savedGoal) {
        val goal = savedGoal ?: return@LaunchedEffect
        prefs.saveSteps(liveSteps)
        val today = LocalDate.now().toString()
        db.stepDao().insertOrUpdate(DailyStepEntity(today, liveSteps, goal))
    }

    Scaffold(
        timeText = { TimeText() }
    ) {
        if (savedGoal == null) {
            WelcomeGoalScreen(
                onGoalSet = { newGoal ->
                    scope.launch { prefs.saveGoal(newGoal) }
                }
            )
        } else {
            DashboardScreen(
                steps = liveSteps,
                goal = savedGoal!!,
                weeklyData = weeklyData.reversed(),
                onResetGoal = {
                    scope.launch { prefs.saveGoal(0) }
                }
            )
        }
    }
}

@Composable
fun WelcomeGoalScreen(onGoalSet: (Int) -> Unit) {
    var inputGoal by remember { mutableStateOf("5000") }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text(
                text = "Bienvenido,\n¿cuál es tu meta de hoy?",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(Color.DarkGray, shape = MaterialTheme.shapes.medium)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = inputGoal,
                    onValueChange = { if (it.all { char -> char.isDigit() }) inputGoal = it },
                    textStyle = TextStyle(
                        color = Color.Cyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(Color.Cyan),
                    singleLine = true
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val parsed = inputGoal.toIntOrNull() ?: 5000
                    if (parsed > 0) onGoalSet(parsed)
                },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Guardar Meta")
            }
        }
    }
}

@Composable
fun DashboardScreen(
    steps: Int,
    goal: Int,
    weeklyData: List<DailyStepEntity>,
    onResetGoal: () -> Unit
) {
    val currentAnimal = getAnimalForSteps(steps)
    val remaining = (goal - steps).coerceAtLeast(0)

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = currentAnimal.emoji, fontSize = 36.sp)
                Text(
                    text = "Nivel: ${currentAnimal.name}",
                    fontSize = 13.sp,
                    color = Color.Cyan,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$steps",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (remaining > 0) "Faltan $remaining de $goal" else "¡Meta lograda!",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Progreso Semanal",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            WeeklyBarChart(data = weeklyData)
        }
    }
}

@Composable
fun WeeklyBarChart(data: List<DailyStepEntity>) {
    val maxSteps = (data.maxOfOrNull { maxOf(it.steps, it.goal) } ?: 10000).coerceAtLeast(1)

    Canvas(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(70.dp)
            .padding(top = 6.dp)
    ) {
        if (data.isEmpty()) return@Canvas

        val barWidth = size.width / (data.size * 2f)
        data.forEachIndexed { index, day ->
            val barHeight = (day.steps.toFloat() / maxSteps) * size.height
            val xOffset = index * (barWidth * 2f) + (barWidth / 2f)
            val yOffset = size.height - barHeight

            drawRect(
                color = if (day.steps >= day.goal) Color.Green else Color.Cyan,
                topLeft = Offset(xOffset, yOffset),
                size = Size(barWidth, barHeight)
            )
        }
    }
}