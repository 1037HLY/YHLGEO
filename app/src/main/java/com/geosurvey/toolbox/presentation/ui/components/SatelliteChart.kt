package com.geosurvey.toolbox.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.geosurvey.toolbox.presentation.SatelliteDetail
import kotlin.math.*

@Composable
fun SatelliteChart(
    satellites: List<SatelliteDetail>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(280.dp)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = min(size.width, size.height) / 2 - 20

            // 绘制外圈
            drawCircle(
                color = Color(0xFF0EA5E9).copy(alpha = 0.3f),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1f)
            )

            // 绘制内圈（50%和75%）
            drawCircle(
                color = Color(0xFF0EA5E9).copy(alpha = 0.15f),
                radius = radius * 0.5f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 0.5f)
            )
            drawCircle(
                color = Color(0xFF0EA5E9).copy(alpha = 0.15f),
                radius = radius * 0.75f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 0.5f)
            )

            // 绘制十字线
            drawLine(
                color = Color(0xFF0EA5E9).copy(alpha = 0.2f),
                start = Offset(centerX - radius, centerY),
                end = Offset(centerX + radius, centerY),
                strokeWidth = 1f
            )
            drawLine(
                color = Color(0xFF0EA5E9).copy(alpha = 0.2f),
                start = Offset(centerX, centerY - radius),
                end = Offset(centerX, centerY + radius),
                strokeWidth = 1f
            )

            // 绘制卫星点
            satellites.forEach { sat ->
                if (sat.elevation > 0) {
                    val elevationRad = sat.elevation / 180f * PI.toFloat()
                    val azimuthRad = (sat.azimuth - 90) / 180f * PI.toFloat()

                    val distance = radius * (1 - elevationRad / (PI.toFloat() / 2))
                    val x = centerX + distance * cos(azimuthRad)
                    val y = centerY - distance * sin(azimuthRad)

                    val color = when {
                        sat.usedInFix -> Color(0xFF10B981)
                        sat.snr > 30 -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }

                    drawCircle(
                        color = color,
                        radius = if (sat.usedInFix) 8f else 5f,
                        center = Offset(x, y)
                    )

                    // 使用 drawIntoCanvas 绘制文字
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            color = 0xFF475569.toInt()
                            textSize = 28f
                            isAntiAlias = true
                        }
                        canvas.nativeCanvas.drawText(
                            sat.prn.toString(),
                            x + 10,
                            y + 4,
                            paint
                        )
                    }
                }
            }

            // 绘制中心标记
            drawCircle(
                color = Color(0xFF0EA5E9),
                radius = 4f,
                center = Offset(centerX, centerY)
            )

            // 绘制方向标记N
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = 0xFF0EA5E9.toInt()
                    textSize = 36f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText(
                    "N",
                    centerX.toFloat(),
                    (centerY - radius + 20).toFloat(),
                    paint
                )
            }
        }
    }
}
