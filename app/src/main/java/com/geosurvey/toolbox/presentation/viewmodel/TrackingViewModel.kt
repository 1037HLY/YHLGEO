package com.geosurvey.toolbox.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geosurvey.toolbox.data.database.AppDatabase
import com.geosurvey.toolbox.data.database.TrackPointEntity
import com.geosurvey.toolbox.domain.service.TrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

data class TrackingUiState(
    val isRecording: Boolean = false,
    val currentTrackId: String? = null,
    val pointCount: Int = 0,
    val trackList: List<TrackSummary> = emptyList(),
    val isLoading: Boolean = false
)

data class TrackSummary(
    val trackId: String,
    val startTime: Date,
    val endTime: Date?,
    val pointCount: Int,
    val distance: Float = 0f
)

class TrackingViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    init {
        loadAllTracks()
    }

    fun startRecording() {
        val trackId = UUID.randomUUID().toString()
        TrackingService.startService(getApplication(), trackId)
        _uiState.value = _uiState.value.copy(
            isRecording = true,
            currentTrackId = trackId
        )
    }

    fun stopRecording() {
        TrackingService.stopService(getApplication())
        _uiState.value = _uiState.value.copy(
            isRecording = false
        )
        loadAllTracks()
    }

    fun loadAllTracks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val trackIds = database.trackPointDao().getAllTrackIds().first()
                val summaries = mutableListOf<TrackSummary>()
                for (id in trackIds) {
                    val count = database.trackPointDao().getPointCount(id)
                    val startTime = database.trackPointDao().getStartTime(id)
                    val endTime = database.trackPointDao().getEndTime(id)
                    if (startTime != null && count > 0) {
                        summaries.add(
                            TrackSummary(
                                trackId = id,
                                startTime = startTime,
                                endTime = endTime,
                                pointCount = count
                            )
                        )
                    }
                }
                _uiState.value = _uiState.value.copy(
                    trackList = summaries,
                    isLoading = false
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun deleteTrack(trackId: String) {
        viewModelScope.launch {
            database.trackPointDao().deleteTrack(trackId)
            loadAllTracks()
        }
    }

    suspend fun getTrackPoints(trackId: String): List<TrackPointEntity> {
        return database.trackPointDao().getTrackPointsSync(trackId)
    }
}
