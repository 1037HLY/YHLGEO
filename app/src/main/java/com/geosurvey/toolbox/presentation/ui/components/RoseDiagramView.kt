package com.geosurvey.toolbox.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.geosurvey.toolbox.utils.GeologicalAnalysisUtils
import kotlin.math.*

@Composable
fun RoseDiagramView(
    strikes: List<Float>,
    modifier: Modifier = Modifier
) {
    val roseData = GeologicalAnalysisUtils.getRoseData(strikes, 10)
    val maxCount = roseData.maxOfOrNull { it.count } ?: 1

    Box(
        modifier = modifier
            .size(300.dp)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = min(size.width, size.height) / 2 - 20

            // 绘制背景圈
            drawCircle(
                color = Color(0xFF475569).copy(alpha = 0.2f),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1f)
            )

            // 绘制刻度圈
            listOf(0.25f, 0.5f, 0.75f, 1f).forEach { scale ->
                drawCircle(
                    color = Color(0xFF475569).copy(alpha = 0.1f),
                    radius = radius * scale,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 0.5f)
                )
            }

            // 绘制每个方向的花瓣
            roseData.forEach { data ->
                val angleRad = Math.toRadians((data.angle - 90).toDouble())
                val length = (data.count.toFloat() / maxCount) * radius

                val x1 = centerX + 2 * cos(angleRad)
                val y1 = centerY + 2 * sin(angleRad)
                val x2 = centerX + length * cos(angleRad)
                val y2 = centerY + length * sin(angleRad)

                val color = when {
                    data.percentage > 30 -> Color(0xFFEF4444)
                    data.percentage > 15 -> Color(0xFFF59E0B)
                    else -> Color(0xFF0EA5E9)
                }

                // 绘制花瓣（使用 Compose Path）
                val halfAngle = Math.toRadians(5.0)
                val path = Path().apply {
                    moveTo(centerX.toFloat(), centerY.toFloat())
                    val p1x = centerX + length * cos(angleRad - halfAngle)
                    val p1y = centerY + length * sin(angleRad - halfAngle)
                    val p2x = centerX + length * cos(angleRad + halfAngle)
                    val p2y = centerY + length * sin(angleRad + halfAngle)
                    lineTo(p1x.toFloat(), p1y.toFloat())
                    lineTo(p2x.toFloat(), p2y.toFloat())
                    close()
                }

                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.8f)
                )

                // 绘制边框
                drawLine(
                    color = color,
                    start = Offset(x1.toFloat(), y1.toFloat()),
                    end = Offset(x2.toFloat(), y2.toFloat()),
                    strokeWidth = 1f
                )
            }

            // 标注方向 - 使用 drawIntoCanvas
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#475569")
                    textSize = 28f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText("N", centerX.toFloat(), (centerY - radius - 10).toFloat(), paint)
                canvas.nativeCanvas.drawText("S", centerX.toFloat(), (centerY + radius + 30).toFloat(), paint)
                canvas.nativeCanvas.drawText("E", (centerX + radius + 20).toFloat(), centerY.toFloat() + 10, paint)
                canvas.nativeCanvas.drawText("W", (centerX - radius - 20).toFloat(), centerY.toFloat() + 10, paint)
            }
        }
    }
}
