package com.geosurvey.toolbox.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import com.geosurvey.toolbox.data.database.PhotoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class WatermarkConfig(
    val showCoordinates: Boolean = true,
    val showTime: Boolean = true,
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
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

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
        // 暂时从UIState中移除历史照片功能
        _uiState.value = _uiState.value.copy(photoList = emptyList())
    }

    fun savePhotoWithWatermark(
        originalBitmap: Bitmap,
        locationName: String = ""
    ) {
        // 模拟保存
        val state = _uiState.value
        val watermarkText = buildWatermarkText(state, locationName)
        
        // 生成模拟路径
        val fileName = "geo_${System.currentTimeMillis()}.jpg"
        val filePath = getApplication().filesDir.absolutePath + "/" + fileName

        // 创建模拟的PhotoEntity
        val photoEntity = PhotoEntity(
            imagePath = filePath,
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

        // 更新状态
        val currentList = _uiState.value.photoList.toMutableList()
        currentList.add(0, photoEntity)
        
        _uiState.value = _uiState.value.copy(
            isTakingPhoto = false,
            lastPhotoPath = filePath,
            note = "",
            photoList = currentList
        )
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

    fun deletePhoto(photoId: Long) {
        val currentList = _uiState.value.photoList.toMutableList()
        currentList.removeAll { it.id == photoId }
        _uiState.value = _uiState.value.copy(photoList = currentList)
    }
}
