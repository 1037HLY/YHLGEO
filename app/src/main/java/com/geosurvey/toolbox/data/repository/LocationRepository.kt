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

    fun getLocationFlow(): Flow<LocationPoint> = callbackFlow {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS未开启，发射一个空位置
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
     * 获取当前GNSS卫星状态 - 修复版本
     */
    private fun getGnssStatus(): GnssInfo {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return GnssInfo()
        }

        try {
            // Android 7.0+ 使用新API
            val gnssStatus = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                locationManager.getGnssStatus()
            } else {
                // 旧版本返回空
                return GnssInfo()
            } ?: return GnssInfo()
            
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
        }
    }

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
