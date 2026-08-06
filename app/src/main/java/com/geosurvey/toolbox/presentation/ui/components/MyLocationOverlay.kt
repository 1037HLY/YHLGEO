package com.geosurvey.toolbox.presentation.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.Projection

class MyLocationOverlay(
    private var location: GeoPoint?,
    private var bearing: Float = 0f
) : Overlay() {

    fun updateLocation(location: GeoPoint?, bearing: Float = 0f) {
        this.location = location
        this.bearing = bearing
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val location = this.location ?: return

        val projection = mapView.projection
        val screenPoint = projection.toPixels(location, null)

        // 绘制蓝色圆点
        val paint = Paint().apply {
            color = 0xFF0EA5E9.toInt()
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        canvas.drawCircle(screenPoint.x.toFloat(), screenPoint.y.toFloat(), 20f, paint)

        // 绘制外圈（脉冲效果）
        val pulsePaint = Paint().apply {
            color = 0xFF0EA5E9.toInt()
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 3f
            alpha = 150
        }
        canvas.drawCircle(screenPoint.x.toFloat(), screenPoint.y.toFloat(), 30f, pulsePaint)

        // 绘制方向箭头
        val arrowPaint = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        canvas.save()
        canvas.translate(screenPoint.x.toFloat(), screenPoint.y.toFloat())
        canvas.rotate(bearing)

        val path = Path().apply {
            moveTo(0f, -30f)
            lineTo(-15f, 10f)
            lineTo(15f, 10f)
            close()
        }
        canvas.drawPath(path, arrowPaint)

        canvas.restore()
    }
}

fun createLocationBitmap(centerX: Float, centerY: Float, radius: Float): Bitmap {
    val bitmap = Bitmap.createBitmap(
        (radius * 2).toInt(),
        (radius * 2).toInt(),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = 0xFF0EA5E9.toInt()
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, radius, paint)
    return bitmap
}
