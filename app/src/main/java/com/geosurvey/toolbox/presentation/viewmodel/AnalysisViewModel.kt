package com.geosurvey.toolbox.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geosurvey.toolbox.data.database.AppDatabase
import com.geosurvey.toolbox.data.database.AttitudeEntity
import com.geosurvey.toolbox.utils.GeologicalAnalysisUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AnalysisUiState(
    val allAttitudes: List<AttitudeEntity> = emptyList(),
    val selectedAttitudes: List<AttitudeEntity> = emptyList(),
    val statistics: GeologicalAnalysisUtils.AttitudeStatistics = 
        GeologicalAnalysisUtils.AttitudeStatistics(0, 0f, 0f, 0f, 0f, 0f, 0f),
    val isLoading: Boolean = false,
    val showAllData: Boolean = true,
    val error: String? = null
)

class AnalysisViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                database.attitudeDao().getAllAttitudes().collect { attitudes ->
                    _uiState.value = _uiState.value.copy(
                        allAttitudes = attitudes,
                        selectedAttitudes = attitudes,
                        isLoading = false
                    )
                    updateStatistics()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载数据失败: ${e.message}"
                )
            }
        }
    }

    fun toggleDataFilter(showAll: Boolean) {
        _uiState.value = _uiState.value.copy(showAllData = showAll)
        if (showAll) {
            _uiState.value = _uiState.value.copy(
                selectedAttitudes = _uiState.value.allAttitudes
            )
        } else {
            // 可以选择部分数据（这里演示选择最近10条）
            val all = _uiState.value.allAttitudes
            val selected = if (all.size > 10) all.take(10) else all
            _uiState.value = _uiState.value.copy(
                selectedAttitudes = selected
            )
        }
        updateStatistics()
    }

    private fun updateStatistics() {
        val selected = _uiState.value.selectedAttitudes
        if (selected.isNotEmpty()) {
            val stats = GeologicalAnalysisUtils.calculateStatistics(selected)
            _uiState.value = _uiState.value.copy(statistics = stats)
        }
    }

    fun getStrikes(): List<Float> {
        return _uiState.value.selectedAttitudes.map { it.strike }
    }

    fun getDips(): List<Float> {
        return _uiState.value.selectedAttitudes.map { it.dip }
    }

    fun getDipDirections(): List<Float> {
        return _uiState.value.selectedAttitudes.map { it.dipDirection }
    }
}
