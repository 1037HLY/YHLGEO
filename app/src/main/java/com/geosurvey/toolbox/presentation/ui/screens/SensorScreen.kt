package com.geosurvey.toolbox.presentation.ui.screens

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SensorScreen() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager }
    
    var accelerometerData by remember { mutableStateOf("等待数据...") }
    var gyroscopeData by remember { mutableStateOf("等待数据...") }
    var magnetometerData by remember { mutableStateOf("等待数据...") }

    DisposableEffect(Unit) {
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        val magnitude = kotlin.math.sqrt(
                            event.values[0] * event.values[0] +
                            event.values[1] * event.values[1] +
                            event.values[2] * event.values[2]
                        )
                        accelerometerData = String.format("%.2f m/s²", magnitude)
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        val magnitude = kotlin.math.sqrt(
                            event.values[0] * event.values[0] +
                            event.values[1] * event.values[1] +
                            event.values[2] * event.values[2]
                        )
                        gyroscopeData = String.format("%.2f rad/s", magnitude)
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        val magnitude = kotlin.math.sqrt(
                            event.values[0] * event.values[0] +
                            event.values[1] * event.values[1] +
                            event.values[2] * event.values[2]
                        )
                        magnetometerData = String.format("%.2f μT", magnitude)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelerometerSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        gyroscopeSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometerSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        Text(
            text = "📊 传感器数据",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "实时加速度计 · 陀螺仪 · 磁力计",
            fontSize = 14.sp,
            color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 加速度计
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF).copy(alpha = 0.85f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "加速度计",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0EA5E9)
                )
                Text(
                    text = accelerometerData,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 陀螺仪
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF).copy(alpha = 0.85f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "陀螺仪",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8B5CF6)
                )
                Text(
                    text = gyroscopeData,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 磁力计
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF).copy(alpha = 0.85f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "磁力计",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF10B981)
                )
                Text(
                    text = magnetometerData,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}
