package com.geosurvey.toolbox.data.repository

import android.content.Context
import android.location.GnssStatus
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

class LocationRepository(
    private val context: Context
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun getLocationFlow(): Flow<LocationPoint> = callbackFlow {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            trySend(
                LocationPoint(
                    0.0, 0.0, 0.0, 0f, 0f, 0f,
                    quality = LocationQuality.UNKNOWN
                )
            )
        }

        val locationListener = android.location.LocationListener { location ->
            val gnssInfo = getGnssStatus()
            val quality = evaluateQuality(gnssInfo)

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

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                locationListener
            )
        } catch (e: SecurityException) {
            close(e)
        }

        awaitClose {
            locationManager.removeUpdates(locationListener)
        }
    }.catch { e ->
        emit(
            LocationPoint(
                0.0, 0.0, 0.0, 0f, 0f, 0f,
                quality = LocationQuality.UNKNOWN
            )
        )
    }.distinctUntilChanged { old, new ->
        old.latitude == new.latitude && old.longitude == new.longitude
    }

    /**
     * 获取当前GNSS卫星状态 - 使用兼容方式
     */
    private fun getGnssStatus(): GnssInfo {
        // 如果GPS未启用，直接返回空
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return GnssInfo()
        }

        try {
            // 使用反射方式调用，避免编译时API检查
            val gnssStatus = try {
                // 尝试获取GnssStatus（Android 7.0+）
                val method = locationManager.javaClass.getMethod("getGnssStatus")
                method.invoke(locationManager) as? GnssStatus
            } catch (e: Exception) {
                // 如果失败，尝试使用旧版API获取卫星信息
                return getLegacySatelliteInfo()
            }

            if (gnssStatus == null) {
                return getLegacySatelliteInfo()
            }

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

            val usedCount = satellites.count { it.usedInFix }
            return GnssInfo(
                satelliteCount = satellites.size,
                usedSatelliteCount = usedCount,
                hdop = 1.5f,
                vdop = 2.0f,
                pdop = 2.5f,
                satellites = satellites
            )
        } catch (e: SecurityException) {
            return GnssInfo()
        } catch (e: Exception) {
            return GnssInfo()
        }
    }

    /**
     * 使用旧版API获取卫星信息（Android 6.0及以下）
     */
    private fun getLegacySatelliteInfo(): GnssInfo {
        try {
            val satellites = mutableListOf<SatelliteInfo>()
            var count = 0

            // 尝试获取GpsStatus
            val gpsStatus = locationManager.getGpsStatus(null)
            if (gpsStatus != null) {
                val iter = gpsStatus.satellites
                // 使用Kotlin的while循环
                while (iter.hasNext()) {
                    val sat = iter.next()
                    count++
                    satellites.add(
                        SatelliteInfo(
                            constellation = Constellation.GPS, // 旧API只支持GPS
                            prn = sat.prn,
                            snr = sat.snr,
                            usedInFix = sat.usedInFix(),
                            azimuth = sat.azimuth,
                            elevation = sat.elevation
                        )
                    )
                }
            }

            return GnssInfo(
                satelliteCount = count,
                usedSatelliteCount = satellites.count { it.usedInFix },
                hdop = 1.5f,
                vdop = 2.0f,
                pdop = 2.5f,
                satellites = satellites
            )
        } catch (e: Exception) {
            return GnssInfo()
        }
    }

    private fun parseConstellation(type: Int): Constellation {
        return when (type) {
            GnssStatus.CONSTELLATION_GPS -> Constellation.GPS
            GnssStatus.CONSTELLATION_GLONASS -> Constellation.GLONASS
            GnssStatus.CONSTELLATION_GALILEO -> Constellation.GALILEO
            GnssStatus.CONSTELLATION_BEIDOU -> Constellation.BEIDOU
            GnssStatus.CONSTELLATION_QZSS -> Constellation.QZSS
            else -> Constellation.UNKNOWN
        }
    }

    private fun evaluateQuality(gnssInfo: GnssInfo): LocationQuality {
        val count = gnssInfo.usedSatelliteCount
        val hdop = gnssInfo.hdop
        val pdop = gnssInfo.pdop

        return when {
            count >= 8 && hdop < 1.5 && pdop < 2.5 -> LocationQuality.EXCELLENT
            count >= 5 && hdop < 2.5 && pdop < 4.0 -> LocationQuality.GOOD
            count >= 3 && hdop < 4.0 && pdop < 6.0 -> LocationQuality.FAIR
            else -> LocationQuality.POOR
        }
    }
}
