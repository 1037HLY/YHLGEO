package com.geosurvey.toolbox.presentation.ui.screens

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

@Composable
fun SensorScreen() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager }
    
    var accelerometerData by remember { mutableStateOf(listOf<Float>()) }
    var gyroscopeData by remember { mutableStateOf(listOf<Float>()) }
    var magnetometerData by remember { mutableStateOf(listOf<Float>()) }
    val maxDataPoints = 60

    DisposableEffect(Unit) {
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        val magnitude = sqrt(
                            event.values[0] * event.values[0] +
                            event.values[1] * event.values[1] +
                            event.values[2] * event.values[2]
                        )
                        accelerometerData = (accelerometerData + magnitude).takeLast(maxDataPoints)
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        val magnitude = sqrt(
                            event.values[0] * event.values[0] +
                            event.values[1] * event.values[1] +
                            event.values[2] * event.values[2]
                        )
                        gyroscopeData = (gyroscopeData + magnitude).takeLast(maxDataPoints)
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        val magnitude = sqrt(
                            event.values[0] * event.values[0] +
                            event.values[1] * event.values[1] +
                            event.values[2] * event.values[2]
                        )
                        magnetometerData = (magnetometerData + magnitude).takeLast(maxDataPoints)
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

        SensorCurveCard(
            title = "加速度计",
            data = accelerometerData,
            color = Color(0xFF0EA5E9),
            unit = "m/s²"
        )

        Spacer(modifier = Modifier.height(12.dp))

        SensorCurveCard(
            title = "陀螺仪",
            data = gyroscopeData,
            color = Color(0xFF8B5CF6),
            unit = "rad/s"
        )

        Spacer(modifier = Modifier.height(12.dp))

        SensorCurveCard(
            title = "磁力计",
            data = magnetometerData,
            color = Color(0xFF10B981),
            unit = "μT"
        )
    }
}

@Composable
fun SensorCurveCard(
    title: String,
    data: List<Float>,
    color: Color,
    unit: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF).copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )
                if (data.isNotEmpty()) {
                    Text(
                        text = "${"%.2f".format(data.last())} $unit",
                        fontSize = 14.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val width = size.width
                val height = size.height
                val padding = 8f

                if (data.size > 1) {
                    val maxValue = data.maxOrNull()?.let { if (it == 0f) 1f else it * 1.2f } ?: 1f
                    val minValue = data.minOrNull() ?: 0f
                    val range = if (maxValue - minValue > 0) maxValue - minValue else 1f

                    val path = Path()
                    val step = (width - padding * 2) / (data.size - 1)

                    for (i in data.indices) {
                        val x = padding + i * step
                        val normalizedValue = (data[i] - minValue) / range
                        val y = height - padding - normalizedValue * (height - padding * 2)
                        if (i == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 3f)
                    )
                } else {
                    // 修复：使用正确的方式绘制文字
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            color = 0xFF94A3B8.toInt()  // 使用Int值
                            textSize = 28f
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        canvas.nativeCanvas.drawText(
                            "等待数据...",
                            width / 2,
                            height / 2 + 10,
                            paint
                        )
                    }
                }
            }
        }
    }
}
