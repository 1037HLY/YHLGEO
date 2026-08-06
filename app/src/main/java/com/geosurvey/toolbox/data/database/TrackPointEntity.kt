package com.geosurvey.toolbox.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "track_points")
data class TrackPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackId: String,           // 轨迹ID，同一次记录共享相同ID
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val bearing: Float,
    val accuracy: Float,
    val timestamp: Date,
    val provider: String,
    val satelliteCount: Int,
    val hdop: Float,
    val pdop: Float
)
