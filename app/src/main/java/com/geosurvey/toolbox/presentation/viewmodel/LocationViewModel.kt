package com.geosurvey.toolbox.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geosurvey.toolbox.domain.model.LocationPoint
import com.geosurvey.toolbox.domain.usecase.GetLocationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LocationUiState(
    val location: LocationPoint? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val satelliteCount: Int = 0,
    val qualityText: String = "等待定位..."
)

class LocationViewModel(
    private val getLocationUseCase: GetLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    init {
        startLocationUpdates()
    }

    fun startLocationUpdates() {
        viewModelScope.launch {
            getLocationUseCase().collect { location ->
                _uiState.update { state ->
                    state.copy(
                        location = location,
                        isLoading = false,
                        satelliteCount = location.gnssInfo?.usedSatelliteCount ?: 0,
                        qualityText = when (location.quality) {
                            com.geosurvey.toolbox.domain.model.LocationQuality.EXCELLENT -> "⭐ 优秀"
                            com.geosurvey.toolbox.domain.model.LocationQuality.GOOD -> "✅ 良好"
                            com.geosurvey.toolbox.domain.model.LocationQuality.FAIR -> "⚠️ 一般"
                            com.geosurvey.toolbox.domain.model.LocationQuality.POOR -> "❌ 较差"
                            else -> "📡 搜索卫星..."
                        }
                    )
                }
            }
        }
    }

    fun refreshLocation() {
        // 可以在这里添加手动刷新逻辑
        startLocationUpdates()
    }
}
