package com.geosurvey.toolbox.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.geosurvey.toolbox.data.database.AttitudeEntity
import kotlin.math.*

@Composable
fun StereonetView(
    attitudes: List<AttitudeEntity>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(300.dp)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = min(size.width, size.height) / 2 - 20

            // 绘制外圈（赤平投影网）
            drawCircle(
                color = Color(0xFF475569).copy(alpha = 0.3f),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1f)
            )

            // 绘制十字线
            drawLine(
                color = Color(0xFF475569).copy(alpha = 0.2f),
                start = Offset(centerX - radius, centerY),
                end = Offset(centerX + radius, centerY),
                strokeWidth = 0.5f
            )
            drawLine(
                color = Color(0xFF475569).copy(alpha = 0.2f),
                start = Offset(centerX, centerY - radius),
                end = Offset(centerX, centerY + radius),
                strokeWidth = 0.5f
            )

            // 绘制内圈（30°, 60°）
            listOf(0.3f, 0.6f).forEach { scale ->
                drawCircle(
                    color = Color(0xFF475569).copy(alpha = 0.15f),
                    radius = radius * scale,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 0.5f)
                )
            }

            // 绘制产状投影点
            attitudes.forEach { attitude ->
                val strikeRad = Math.toRadians(attitude.strike.toDouble())
                val dipRad = Math.toRadians(attitude.dip.toDouble())

                // 赤平投影计算
                val distance = radius * (1 - sin(dipRad))
                val x = centerX + distance * cos(strikeRad)
                val y = centerY - distance * sin(strikeRad)

                val color = when {
                    attitude.dip > 60 -> Color(0xFFEF4444)
                    attitude.dip > 30 -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                }

                drawCircle(
                    color = color,
                    radius = 5f,
                    center = Offset(x.toFloat(), y.toFloat())
                )
            }

            // 标注方向
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#475569")
                    textSize = 28f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText("N", centerX.toFloat(), (centerY - radius - 10).toFloat(), paint)
                drawText("S", centerX.toFloat(), (centerY + radius + 30).toFloat(), paint)
                drawText("E", (centerX + radius + 20).toFloat(), centerY.toFloat() + 10, paint)
                drawText("W", (centerX - radius - 20).toFloat(), centerY.toFloat() + 10, paint)
            }
        }
    }
}
