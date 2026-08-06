package com.geosurvey.toolbox.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drill_samples")
data class DrillSampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sampleNumber: String = "",    // 样本编号
    val depthFrom: String = "",       // 孔深自
    val depthTo: String = "",         // 孔深至
    val sampleLength: String = "",    // 样长
    val coreLength: String = "",      // 岩心长
    val recoveryRate: String = "",    // 采取率
    val weight: String = "",          // 重量
    val name: String = "",            // 名称
    val coreDiameter: String = "",    // 岩心直径
    val description: String = "",     // 描述
    val latitude: Double = 0.0,       // 采集点纬度
    val longitude: Double = 0.0,      // 采集点经度
    val altitude: Double = 0.0,       // 采集点海拔
    val timestamp: Long = System.currentTimeMillis()
)
