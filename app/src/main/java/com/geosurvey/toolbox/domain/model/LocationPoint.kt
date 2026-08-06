package com.geosurvey.toolbox.domain.model

import java.util.Date

/**
 * 融合后的定位点，包含位置、精度和GNSS质量信息
 */
data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val bearing: Float,
    val accuracy: Float,          // 水平精度 (米)
    val timestamp: Date = Date(),
    val provider: String = "gnss",
    val gnssInfo: GnssInfo? = null,
    val quality: LocationQuality = LocationQuality.UNKNOWN
)

/**
 * 定位质量枚举
 */
enum class LocationQuality {
    EXCELLENT,   // 优：卫星多，HDOP小
    GOOD,        // 良：适合一般导航
    FAIR,        // 中：可用，但误差较大
    POOR,        // 差：不建议使用
    UNKNOWN
}
