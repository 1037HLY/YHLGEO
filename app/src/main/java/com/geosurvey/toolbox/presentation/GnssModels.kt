package com.geosurvey.toolbox.presentation

data class SatelliteDetail(
    val prn: Int,                    // 卫星编号
    val constellation: String,       // 星座名称
    val snr: Float,                  // 信噪比 (dB-Hz)
    val azimuth: Float,              // 方位角 (度)
    val elevation: Float,            // 仰角 (度)
    val usedInFix: Boolean           // 是否用于定位
)

data class GnssStatusData(
    val satellites: List<SatelliteDetail>,
    val usedCount: Int,
    val totalCount: Int,
    val hdop: Float,
    val vdop: Float,
    val pdop: Float
)
