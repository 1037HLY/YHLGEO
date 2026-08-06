package com.geosurvey.toolbox.data.repository

import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.geosurvey.toolbox.domain.model.Constellation
import com.geosurvey.toolbox.domain.model.GnssInfo
import com.geosurvey.toolbox.domain.model.LocationPoint
import com.geosurvey.toolbox.domain.model.LocationQuality
import com.geosurvey.toolbox.domain.model.SatelliteInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class LocationRepository(
    private val context: Context
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    /**
     * 创建定位数据流
     * 这是整个定位系统的核心，通过callbackFlow实时发射定位数据
     */
    fun getLocationFlow(): Flow<LocationPoint> = callbackFlow {
        // 检查GPS是否开启
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 可以在这里发射一个错误状态，或等待GPS开启
            // 简单起见，我们在这里不处理，由上层UI提示
        }

        // 1. 监听位置变化（Fused Location Provider的替代，直接使用原生GPS）
        val locationListener = android.location.LocationListener { location ->
            // 获取最新的GNSS状态
            val gnssInfo = getGnssStatus()
            val quality = evaluateQuality(gnssInfo)

            // 转换成我们的LocationPoint
            val point = LocationPoint(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                speed = location.speed,
                bearing = location.bearing,
                accuracy = location.accuracy,
                timestamp = java.util.Date(location.time),
                provider = location.provider ?: "gps",
                gnssInfo = gnssInfo,
                quality = quality
            )
            trySend(point)
        }

        // 注册位置监听（只使用GPS，网络定位作为备选可后续扩展）
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,  // 最小更新间隔 1秒
                1f,     // 最小更新距离 1米
                locationListener
            )
        } catch (e: SecurityException) {
            // 没有权限，上层处理
            close(e)
        }

        // 当Flow被取消时，移除监听
        awaitClose {
            locationManager.removeUpdates(locationListener)
        }
    }.catch { e ->
        // 捕获异常，确保Flow不会崩溃
        emit(
            LocationPoint(
                0.0, 0.0, 0.0, 0f, 0f, 0f,
                quality = LocationQuality.UNKNOWN
            )
        )
    }.distinctUntilChanged { old, new ->
        // 如果位置没有明显变化，不发射新数据，减少UI刷新
        old.latitude == new.latitude && old.longitude == new.longitude
    }

    /**
     * 获取当前GNSS卫星状态
     */
    private fun getGnssStatus(): GnssInfo {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return GnssInfo()
        }

        try {
            // 获取GPS状态
            val gnssStatus = locationManager.getGnssStatus() ?: return GnssInfo()
            val satellites = mutableListOf<SatelliteInfo>()

            for (i in 0 until gnssStatus.satelliteCount) {
                val prn = gnssStatus.getSvid(i)
                val constellation = parseConstellation(gnssStatus.getConstellationType(i))
                val snr = gnssStatus.getCn0DbHz(i)
                val usedInFix = gnssStatus.usedInFix(i)
                val azimuth = gnssStatus.getAzimuthDegrees(i)
                val elevation = gnssStatus.getElevationDegrees(i)

                satellites.add(
                    SatelliteInfo(
                        constellation = constellation,
                        prn = prn,
                        snr = snr,
                        usedInFix = usedInFix,
                        azimuth = azimuth,
                        elevation = elevation
                    )
                )
            }

            // 计算使用的卫星数量和总数量
            val usedCount = satellites.count { it.usedInFix }
            // HDOP等数据在GnssStatus中无法直接获取，需要通过NMEA或测量数据计算
            // 这里我们返回模拟值，真实计算需要解析NMEA语句或使用GnssMeasurements
            // 在实际项目中，可结合NmeaListener解析GGA语句获取HDOP
            return GnssInfo(
                satelliteCount = satellites.size,
                usedSatelliteCount = usedCount,
                hdop = 1.5f, // 模拟值，实际应从NMEA中解析
                vdop = 2.0f,
                pdop = 2.5f,
                satellites = satellites
            )
        } catch (e: SecurityException) {
            return GnssInfo()
        }
    }

    /**
     * 解析星座类型
     */
    private fun parseConstellation(type: Int): Constellation {
        return when (type) {
            android.location.GnssStatus.CONSTELLATION_GPS -> Constellation.GPS
            android.location.GnssStatus.CONSTELLATION_GLONASS -> Constellation.GLONASS
            android.location.GnssStatus.CONSTELLATION_GALILEO -> Constellation.GALILEO
            android.location.GnssStatus.CONSTELLATION_BEIDOU -> Constellation.BEIDOU
            android.location.GnssStatus.CONSTELLATION_QZSS -> Constellation.QZSS
            else -> Constellation.UNKNOWN
        }
    }

    /**
     * 定位质量评估算法
     */
    private fun evaluateQuality(gnssInfo: GnssInfo): LocationQuality {
        val count = gnssInfo.usedSatelliteCount
        val hdop = gnssInfo.hdop
        val pdop = gnssInfo.pdop

        // 根据卫星数量、HDOP、PDOP综合判断
        return when {
            count >= 8 && hdop < 1.5 && pdop < 2.5 -> LocationQuality.EXCELLENT
            count >= 5 && hdop < 2.5 && pdop < 4.0 -> LocationQuality.GOOD
            count >= 3 && hdop < 4.0 && pdop < 6.0 -> LocationQuality.FAIR
            else -> LocationQuality.POOR
        }
    }
}
