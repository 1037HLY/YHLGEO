package com.geosurvey.toolbox.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geosurvey.toolbox.data.database.AppDatabase
import com.geosurvey.toolbox.data.database.PhotoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class WatermarkConfig(
    val showCoordinates: Boolean = true,
    val showTime: Boolean = true,
    val showLocation: Boolean = true,
    val showAttitude: Boolean = true,
    val showNote: Boolean = true,
    val fontSize: Float = 40f,
    val position: WatermarkPosition = WatermarkPosition.BOTTOM_RIGHT,
    val opacity: Float = 0.7f
)

enum class WatermarkPosition {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
}

data class CameraUiState(
    val isTakingPhoto: Boolean = false,
    val lastPhotoPath: String? = null,
    val photoList: List<PhotoEntity> = emptyList(),
    val currentLocation: Location? = null,
    val strike: Float? = null,
    val dip: Float? = null,
    val dipDirection: Float? = null,
    val note: String = "",
    val watermarkConfig: WatermarkConfig = WatermarkConfig(),
    val error: String? = null
)

class CameraViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
    }

    fun updateLocation(location: Location) {
        _uiState.value = _uiState.value.copy(currentLocation = location)
    }

    fun updateAttitude(strike: Float?, dip: Float?, dipDirection: Float?) {
        _uiState.value = _uiState.value.copy(
            strike = strike,
            dip = dip,
            dipDirection = dipDirection
        )
    }

    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun updateWatermarkConfig(config: WatermarkConfig) {
        _uiState.value = _uiState.value.copy(watermarkConfig = config)
    }

    fun loadPhotos() {
        viewModelScope.launch {
            try {
                database.photoDao().getAllPhotos().collect { photos ->
                    _uiState.value = _uiState.value.copy(photoList = photos)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun savePhotoWithWatermark(
        originalBitmap: Bitmap,
        locationName: String = ""
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isTakingPhoto = true)

                val state = _uiState.value
                val config = state.watermarkConfig

                val watermarkText = buildWatermarkText(state, locationName)
                val watermarkedBitmap = addWatermark(originalBitmap, watermarkText, config)

                val fileName = "geo_${System.currentTimeMillis()}.jpg"
                val file = File(getApplication().filesDir, fileName)

                // 简化：直接保存到文件，使用 Java 原生方式
                val fos = FileOutputStream(file)
                watermarkedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.flush()
                fos.close()

                val photoEntity = PhotoEntity(
                    imagePath = file.absolutePath,
                    latitude = state.currentLocation?.latitude ?: 0.0,
                    longitude = state.currentLocation?.longitude ?: 0.0,
                    altitude = state.currentLocation?.altitude ?: 0.0,
                    timestamp = System.currentTimeMillis(),
                    strike = state.strike,
                    dip = state.dip,
                    dipDirection = state.dipDirection,
                    note = state.note,
                    watermarkText = watermarkText
                )
                database.photoDao().insert(photoEntity)

                _uiState.value = _uiState.value.copy(
                    isTakingPhoto = false,
                    lastPhotoPath = file.absolutePath,
                    note = ""
                )
                loadPhotos()

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isTakingPhoto = false,
                    error = "保存照片失败: ${e.message}"
                )
            }
        }
    }

    private fun buildWatermarkText(state: CameraUiState, locationName: String): String {
        val lines = mutableListOf<String>()
        val config = state.watermarkConfig
        val loc = state.currentLocation

        if (config.showCoordinates && loc != null) {
            lines.add("📍 %.6f, %.6f".format(loc.latitude, loc.longitude))
        }

        if (config.showTime) {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            lines.add("🕐 ${format.format(Date())}")
        }

        if (config.showLocation && locationName.isNotEmpty()) {
            lines.add("🏔️ $locationName")
        }

        if (config.showAttitude) {
            val strike = state.strike
            val dip = state.dip
            val dipDir = state.dipDirection
            if (strike != null && dip != null) {
                lines.add("📐 走向: %.1f° 倾角: %.1f°".format(strike, dip))
                if (dipDir != null) {
                    lines.add("📐 倾向: %.1f°".format(dipDir))
                }
            }
        }

        if (config.showNote && state.note.isNotEmpty()) {
            lines.add("📝 ${state.note}")
        }

        return lines.joinToString("\n")
    }

    private fun addWatermark(bitmap: Bitmap, text: String, config: WatermarkConfig): Bitmap {
        val resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)

        val paint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = config.fontSize
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
            alpha = (config.opacity * 255).toInt()
            textAlign = Paint.Align.LEFT
            setShadowLayer(8f, 0f, 0f, android.graphics.Color.BLACK)
        }

        val lines = text.split("\n")
        var textHeight = 0f
        val textWidths = lines.map { line ->
            val w = paint.measureText(line)
            textHeight += paint.textSize + 10
            w
        }
        val maxWidth = textWidths.maxOrNull() ?: 0f

        val padding = 40f
        val x = when (config.position) {
            WatermarkPosition.TOP_LEFT, WatermarkPosition.BOTTOM_LEFT, WatermarkPosition.CENTER -> padding
            WatermarkPosition.TOP_RIGHT, WatermarkPosition.BOTTOM_RIGHT -> canvas.width - maxWidth - padding
        }
        val y = when (config.position) {
            WatermarkPosition.TOP_LEFT, WatermarkPosition.TOP_RIGHT -> padding + paint.textSize
            WatermarkPosition.BOTTOM_LEFT, WatermarkPosition.BOTTOM_RIGHT -> canvas.height - padding
            WatermarkPosition.CENTER -> canvas.height / 2f + (lines.size * paint.textSize) / 2f
        }

        // 半透明背景
        val bgPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            alpha = (config.opacity * 0.3f * 255).toInt()
            isAntiAlias = true
        }
        val bgX = x - 20
        val bgY = y - paint.textSize - 20
        val bgWidth = maxWidth + 40
        val bgHeight = lines.size * (paint.textSize + 10) + 40
        canvas.drawRoundRect(bgX, bgY, bgX + bgWidth, bgY + bgHeight, 20f, 20f, bgPaint)

        var currentY = y
        for (line in lines) {
            val lineX = when (config.position) {
                WatermarkPosition.TOP_LEFT, WatermarkPosition.BOTTOM_LEFT, WatermarkPosition.CENTER -> x
                WatermarkPosition.TOP_RIGHT, WatermarkPosition.BOTTOM_RIGHT -> canvas.width - paint.measureText(line) - padding
            }
            canvas.drawText(line, lineX, currentY, paint)
            currentY += paint.textSize + 10
        }

        return resultBitmap
    }

    fun deletePhoto(photoId: Long) {
        viewModelScope.launch {
            try {
                val photo = database.photoDao().getPhoto(photoId)
                photo?.let {
                    File(it.imagePath).delete()
                }
                database.photoDao().deletePhoto(photoId)
                loadPhotos()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
