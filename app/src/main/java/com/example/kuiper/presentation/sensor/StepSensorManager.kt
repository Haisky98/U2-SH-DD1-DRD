package com.example.kuiper.presentation.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class StepSensorManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    fun observeSteps(): Flow<Int> = callbackFlow {
        var initialSteps = -1

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val totalSinceBoot = event?.values?.get(0)?.toInt() ?: return
                if (initialSteps == -1) initialSteps = totalSinceBoot
                val sessionSteps = totalSinceBoot - initialSteps
                trySend(sessionSteps)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (stepSensor != null) {
            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            trySend(0)
        }

        awaitClose { sensorManager.unregisterListener(listener) }
    }
}