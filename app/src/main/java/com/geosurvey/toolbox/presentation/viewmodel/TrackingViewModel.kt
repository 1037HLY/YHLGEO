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

/**
 * 轨迹记录UI状态
 */
data class TrackingUiState(
    val isRecording: Boolean = false,
    val currentTrackId: String? = null,
    val pointCount: Int = 0,
    val trackList: List<TrackSummary> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * 轨迹摘要信息
 */
data class TrackSummary(
    val trackId: String,
    val startTime: Long,
    val endTime: Long?,
    val pointCount: Int,
    val distance: Float = 0f
)

/**
 * 轨迹记录ViewModel
 * 负责管理轨迹记录状态和历史轨迹数据
 */
class TrackingViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    init {
        loadAllTracks()
    }

    /**
     * 开始记录轨迹
     * 生成新的轨迹ID并启动前台服务
     */
    fun startRecording() {
        val trackId = UUID.randomUUID().toString()
        TrackingService.startService(getApplication(), trackId)
        _uiState.value = _uiState.value.copy(
            isRecording = true,
            currentTrackId = trackId
        )
    }

    /**
     * 停止记录轨迹
     * 停止前台服务并刷新轨迹列表
     */
    fun stopRecording() {
        TrackingService.stopService(getApplication())
        _uiState.value = _uiState.value.copy(
            isRecording = false
        )
        loadAllTracks()
    }

    /**
     * 加载所有历史轨迹
     * 从数据库读取所有轨迹ID并生成摘要列表
     */
    fun loadAllTracks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val trackIds = database.trackPointDao().getAllTrackIds().first()
                val summaries = mutableListOf<TrackSummary>()
                for (id in trackIds) {
                    val count = database.trackPointDao().getPointCount(id)
                    val startTime = database.trackPointDao().getStartTimeMillis(id)
                    val endTime = database.trackPointDao().getEndTimeMillis(id)
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

    /**
     * 删除指定轨迹
     */
    fun deleteTrack(trackId: String) {
        viewModelScope.launch {
            database.trackPointDao().deleteTrack(trackId)
            loadAllTracks()
        }
    }

    /**
     * 获取指定轨迹的所有点
     */
    suspend fun getTrackPoints(trackId: String): List<TrackPointEntity> {
        return database.trackPointDao().getTrackPointsSync(trackId)
    }
}
