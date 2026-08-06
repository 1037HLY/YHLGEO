package com.geosurvey.toolbox.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geosurvey.toolbox.data.database.AppDatabase
import com.geosurvey.toolbox.data.database.AttitudeEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

data class AttitudeUiState(
    val strike: Float = 0f,
    val dip: Float = 0f,
    val dipDirection: Float = 0f,
    val isMeasuring: Boolean = false,
    val history: List<AttitudeEntity> = emptyList(),
    val currentLocation: Location? = null,
    val note: String = "",
    val accuracy: Float = 0f,
    val error: String? = null
)

class AttitudeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _uiState = MutableStateFlow(AttitudeUiState())
    val uiState: StateFlow<AttitudeUiState> = _uiState.asStateFlow()

    private val sensorListener = object : SensorEventListener {
        private val gravity = FloatArray(3)
        private val geomagnetic = FloatArray(3)
        private val rotationMatrix = FloatArray(9)
        private val orientation = FloatArray(3)

        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(event.values, 0, gravity, 0, 3)
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                }
            }

            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                SensorManager.getOrientation(rotationMatrix, orientation)

                val azimuth = orientation[0]
                val pitch = orientation[1]
                val roll = orientation[2]

                var strike = Math.toDegrees(azimuth.toDouble()).toFloat()
                val dip = Math.toDegrees(pitch.toDouble()).toFloat()
                var dipDirection = (strike + 90) % 360

                if (strike < 0) strike += 360f
                if (dipDirection < 0) dipDirection += 360f

                _uiState.value = _uiState.value.copy(
                    strike = strike,
                    dip = abs(dip),
                    dipDirection = dipDirection,
                    isMeasuring = true
                )
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    init {
        loadHistory()
        startSensor()
    }

    private fun startSensor() {
        accelerometerSensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometerSensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopSensor() {
        sensorManager.unregisterListener(sensorListener)
    }

    fun updateLocation(location: Location) {
        _uiState.value = _uiState.value.copy(currentLocation = location)
    }

    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun saveAttitude() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val entity = AttitudeEntity(
                    strike = state.strike,
                    dip = state.dip,
                    dipDirection = state.dipDirection,
                    latitude = state.currentLocation?.latitude ?: 0.0,
                    longitude = state.currentLocation?.longitude ?: 0.0,
                    altitude = state.currentLocation?.altitude ?: 0.0,
                    timestamp = System.currentTimeMillis(),
                    note = state.note,
                    accuracy = state.accuracy
                )
                database.attitudeDao().insert(entity)
                loadHistory()
                _uiState.value = _uiState.value.copy(note = "")
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(error = "保存失败: ${e.message}")
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                database.attitudeDao().getAllAttitudes().collect { history ->
                    _uiState.value = _uiState.value.copy(history = history)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteAttitude(id: Long) {
        viewModelScope.launch {
            database.attitudeDao().deleteAttitude(id)
            loadHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSensor()
    }
}
