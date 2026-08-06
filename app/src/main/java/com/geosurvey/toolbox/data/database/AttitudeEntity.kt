package com.geosurvey.toolbox.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attitudes")
data class AttitudeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val strike: Float,          // 走向 (0-360°)
    val dip: Float,             // 倾角 (0-90°)
    val dipDirection: Float,    // 倾向 (0-360°)
    val latitude: Double,       // 测量时纬度
    val longitude: Double,      // 测量时经度
    val altitude: Double,       // 测量时海拔
    val timestamp: Long,        // 测量时间
    val note: String = "",      // 备注
    val accuracy: Float = 0f    // 测量精度
)
