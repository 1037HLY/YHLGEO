package com.geosurvey.toolbox.domain.model

import java.util.Date

/**
 * GNSS原始信息数据类
 * 对应系统的GnssStatus和GnssMeasurements
 */
data class GnssInfo(
    val timestamp: Date = Date(),
    val satelliteCount: Int = 0,
    val usedSatelliteCount: Int = 0,
    val hdop: Float = 0f,    // 水平精度因子
    val vdop: Float = 0f,    // 垂直精度因子
    val pdop: Float = 0f,    // 位置精度因子
    val satellites: List<SatelliteInfo> = emptyList()
)

/**
 * 单颗卫星信息
 */
data class SatelliteInfo(
    val constellation: Constellation, // 星座类型
    val prn: Int,                     // 卫星编号
    val snr: Float,                   // 信噪比 (dB-Hz)
    val usedInFix: Boolean = false,   // 是否用于定位
    val azimuth: Float,               // 方位角 (度)
    val elevation: Float              // 仰角 (度)
)

/**
 * 卫星星座枚举
 */
enum class Constellation {
    GPS, GLONASS, GALILEO, BEIDOU, QZSS, UNKNOWN
}
