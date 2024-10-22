package com.example.myapplicationgemini

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SensorPlusScreen() {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    // Variables para almacenar los valores del acelerómetro y giroscopio
    var accelerometerValues by remember { mutableStateOf(listOf(0.0, 0.0, 0.0)) }
    var gyroscopeValues by remember { mutableStateOf(listOf(0.0, 0.0, 0.0)) }
    var isTorchOn by remember { mutableStateOf(false) }

    // Control de la linterna
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = cameraManager.cameraIdList[0]

    // Listener de eventos del acelerómetro y giroscopio
    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        accelerometerValues = listOf(event.values[0].toDouble(), event.values[1].toDouble(), event.values[2].toDouble())
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        gyroscopeValues = listOf(event.values[0].toDouble(), event.values[1].toDouble(), event.values[2].toDouble())
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    // Registrar y desregistrar los sensores usando DisposableEffect
    DisposableEffect(Unit) {
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(sensorEventListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)  // Limpiar al desregistrar el listener
        }
    }

    // Función para encender/apagar la linterna
    fun toggleTorch() {
        try {
            if (isTorchOn) {
                cameraManager.setTorchMode(cameraId, false)
            } else {
                cameraManager.setTorchMode(cameraId, true)
            }
            isTorchOn = !isTorchOn
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Mostrar los valores en la interfaz
    val accelerometerText = "Acelerómetro: ${accelerometerValues.map { it.toString().take(4) }}"
    val gyroscopeText = "Giroscopio: ${gyroscopeValues.map { it.toString().take(4) }}"

    // UI en Jetpack Compose
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = accelerometerText, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = gyroscopeText, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Botón para encender/apagar la linterna
        Button(onClick = { toggleTorch() }) {
            Text(if (isTorchOn) "Apagar Linterna" else "Encender Linterna")
        }
    }
}
