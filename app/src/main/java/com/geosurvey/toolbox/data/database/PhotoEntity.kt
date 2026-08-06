package com.geosurvey.toolbox.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String,          // 照片本地路径
    val latitude: Double,           // 拍摄时纬度
    val longitude: Double,          // 拍摄时经度
    val altitude: Double,           // 拍摄时海拔
    val timestamp: Long,            // 拍摄时间
    val strike: Float? = null,      // 走向（可选）
    val dip: Float? = null,         // 倾角（可选）
    val dipDirection: Float? = null,// 倾向（可选）
    val note: String? = null,       // 备注
    val watermarkText: String? = null // 水印内容
)
