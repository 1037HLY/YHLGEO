package com.geosurvey.toolbox.presentation.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geosurvey.toolbox.data.database.AppDatabase
import com.geosurvey.toolbox.data.database.DrillSampleEntity
import com.geosurvey.toolbox.data.database.SampleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SampleUiState(
    val normalSamples: List<SampleEntity> = emptyList(),
    val drillSamples: List<DrillSampleEntity> = emptyList(),
    val currentLocation: Location? = null,
    val isLoading: Boolean = false,
    val editingNormalSample: SampleEntity? = null,
    val editingDrillSample: DrillSampleEntity? = null,
    val showNormalDialog: Boolean = false,
    val showDrillDialog: Boolean = false
)

class SampleViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val _uiState = MutableStateFlow(SampleUiState())
    val uiState: StateFlow<SampleUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun updateLocation(location: Location) {
        _uiState.value = _uiState.value.copy(currentLocation = location)
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                database.sampleDao().getAllSamples().collect { normal ->
                    _uiState.value = _uiState.value.copy(normalSamples = normal)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                database.drillSampleDao().getAllSamples().collect { drill ->
                    _uiState.value = _uiState.value.copy(drillSamples = drill)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun addNormalSample(sample: SampleEntity) {
        viewModelScope.launch {
            try {
                database.sampleDao().insert(sample)
                loadAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateNormalSample(sample: SampleEntity) {
        viewModelScope.launch {
            try {
                database.sampleDao().update(sample)
                loadAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteNormalSample(id: Long) {
        viewModelScope.launch {
            try {
                database.sampleDao().deleteSample(id)
                loadAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addDrillSample(sample: DrillSampleEntity) {
        viewModelScope.launch {
            try {
                database.drillSampleDao().insert(sample)
                loadAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateDrillSample(sample: DrillSampleEntity) {
        viewModelScope.launch {
            try {
                database.drillSampleDao().update(sample)
                loadAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteDrillSample(id: Long) {
        viewModelScope.launch {
            try {
                database.drillSampleDao().deleteSample(id)
                loadAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startEditNormal(sample: SampleEntity) {
        _uiState.value = _uiState.value.copy(
            editingNormalSample = sample,
            showNormalDialog = true
        )
    }

    fun startEditDrill(sample: DrillSampleEntity) {
        _uiState.value = _uiState.value.copy(
            editingDrillSample = sample,
            showDrillDialog = true
        )
    }

    fun closeDialogs() {
        _uiState.value = _uiState.value.copy(
            showNormalDialog = false,
            showDrillDialog = false,
            editingNormalSample = null,
            editingDrillSample = null
        )
    }

    fun exportSamples(type: String): String {
        val sb = StringBuilder()
        when (type) {
            "normal" -> {
                sb.append("样本类型,编号,名称,重量,描述,经度,纬度,海拔,时间\n")
                _uiState.value.normalSamples.forEach {
                    sb.append("${it.sampleType},${it.sampleNumber},${it.name},${it.weight},${it.description},${it.longitude},${it.latitude},${it.altitude},${it.timestamp}\n")
                }
            }
            "drill" -> {
                sb.append("样本编号,孔深自,孔深至,样长,岩心长,采取率,重量,名称,岩心直径,描述,经度,纬度,海拔,时间\n")
                _uiState.value.drillSamples.forEach {
                    sb.append("${it.sampleNumber},${it.depthFrom},${it.depthTo},${it.sampleLength},${it.coreLength},${it.recoveryRate},${it.weight},${it.name},${it.coreDiameter},${it.description},${it.longitude},${it.latitude},${it.altitude},${it.timestamp}\n")
                }
            }
            "all" -> {
                sb.append("=== 普通样本 ===\n")
                sb.append("样本类型,编号,名称,重量,描述,经度,纬度,海拔,时间\n")
                _uiState.value.normalSamples.forEach {
                    sb.append("${it.sampleType},${it.sampleNumber},${it.name},${it.weight},${it.description},${it.longitude},${it.latitude},${it.altitude},${it.timestamp}\n")
                }
                sb.append("\n=== 钻孔样本 ===\n")
                sb.append("样本编号,孔深自,孔深至,样长,岩心长,采取率,重量,名称,岩心直径,描述,经度,纬度,海拔,时间\n")
                _uiState.value.drillSamples.forEach {
                    sb.append("${it.sampleNumber},${it.depthFrom},${it.depthTo},${it.sampleLength},${it.coreLength},${it.recoveryRate},${it.weight},${it.name},${it.coreDiameter},${it.description},${it.longitude},${it.latitude},${it.altitude},${it.timestamp}\n")
                }
            }
        }
        return sb.toString()
    }
}
