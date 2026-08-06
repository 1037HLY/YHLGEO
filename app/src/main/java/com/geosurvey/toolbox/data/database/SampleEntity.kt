package com.geosurvey.toolbox.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "samples")
data class SampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sampleType: String = "",      // 样本类型
    val sampleNumber: String = "",    // 编号
    val name: String = "",            // 名称
    val weight: String = "",          // 重量
    val description: String = "",     // 描述
    val latitude: Double = 0.0,       // 采集点纬度
    val longitude: Double = 0.0,      // 采集点经度
    val altitude: Double = 0.0,       // 采集点海拔
    val timestamp: Long = System.currentTimeMillis()
)
