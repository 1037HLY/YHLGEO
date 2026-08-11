package com.geosurvey.toolbox.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat

class LocationHelper(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var locationListener: LocationListener? = null
    private var gnssStatusListener: GnssStatus.Callback? = null
    private var onLocationUpdate: ((Location) -> Unit)? = null
    private var onGnssStatusUpdate: ((GnssStatusData) -> Unit)? = null
    private var isListening = false

    // 检查是否有定位权限
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startLocationUpdates(
        onLocationUpdate: (Location) -> Unit,
        onGnssStatusUpdate: (GnssStatusData) -> Unit
    ) {
        this.onLocationUpdate = onLocationUpdate
        this.onGnssStatusUpdate = onGnssStatusUpdate

        if (isListening) {
            return
        }

        // 检查权限
        if (!hasLocationPermission()) {
            // 没有权限，发送空位置
            onLocationUpdate(Location(LocationManager.GPS_PROVIDER))
            return
        }

        // 检查GPS是否开启
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS未开启，发送空位置
            onLocationUpdate(Location(LocationManager.GPS_PROVIDER))
            return
        }

        try {
            // 位置监听
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    onLocationUpdate(location)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

                @Suppress("DEPRECATION")
                override fun onProviderEnabled(provider: String) {
                    // 提供者启用
                }

                @Suppress("DEPRECATION")
                override fun onProviderDisabled(provider: String) {
                    // 提供者禁用
                }
            }

            // 请求位置更新
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                locationListener!!
            )

            // 同时请求网络定位作为备选
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000L,
                    10f,
                    locationListener!!
                )
            } catch (e: Exception) {
                // 网络定位可能不可用，忽略
            }

            // GNSS状态监听 (Android 7.0+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                gnssStatusListener = object : GnssStatus.Callback() {
                    override fun onSatelliteStatusChanged(status: GnssStatus) {
                        val data = parseGnssStatus(status)
                        onGnssStatusUpdate(data)
                    }
                }
                locationManager.registerGnssStatusCallback(gnssStatusListener!!)
            } else {
                // 旧版本使用GpsStatus
                @Suppress("DEPRECATION")
                locationManager.addGpsStatusListener { event ->
                    if (event == 1) { // GPS_EVENT_SATELLITE_STATUS
                        @Suppress("DEPRECATION")
                        val gpsStatus = locationManager.getGpsStatus(null)
                        if (gpsStatus != null) {
                            val data = parseGpsStatus(gpsStatus)
                            onGnssStatusUpdate(data)
                        }
                    }
                }
            }

            isListening = true

            // 发送一个初始位置（如果有缓存位置）
            try {
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                lastLocation?.let {
                    onLocationUpdate(it)
                }
            } catch (e: Exception) {
                // 忽略
            }

        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopLocationUpdates() {
        try {
            locationListener?.let {
                locationManager.removeUpdates(it)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                gnssStatusListener?.let {
                    locationManager.unregisterGnssStatusCallback(it)
                }
            }
            isListening = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    private fun parseGpsStatus(status: android.location.GpsStatus): GnssStatusData {
        val satellites = mutableListOf<SatelliteDetail>()
        for (sat in status.satellites) {
            satellites.add(
                SatelliteDetail(
                    prn = sat.prn,
                    constellation = "GPS",
                    snr = sat.snr,
                    azimuth = sat.azimuth,
                    elevation = sat.elevation,
                    usedInFix = sat.usedInFix()
                )
            )
        }
        return GnssStatusData(
            satellites = satellites,
            usedCount = satellites.count { it.usedInFix },
            totalCount = satellites.size,
            hdop = 1.5f,
            vdop = 2.0f,
            pdop = 2.5f
        )
    }

    private fun parseGnssStatus(status: GnssStatus): GnssStatusData {
        val satellites = mutableListOf<SatelliteDetail>()
        for (i in 0 until status.satelliteCount) {
            val constellation = when (status.getConstellationType(i)) {
                GnssStatus.CONSTELLATION_GPS -> "GPS"
                GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
                GnssStatus.CONSTELLATION_GALILEO -> "Galileo"
                GnssStatus.CONSTELLATION_BEIDOU -> "北斗"
                GnssStatus.CONSTELLATION_QZSS -> "QZSS"
                else -> "未知"
            }
            satellites.add(
                SatelliteDetail(
                    prn = status.getSvid(i),
                    constellation = constellation,
                    snr = status.getCn0DbHz(i),
                    azimuth = status.getAzimuthDegrees(i),
                    elevation = status.getElevationDegrees(i),
                    usedInFix = status.usedInFix(i)
                )
            )
        }
        return GnssStatusData(
            satellites = satellites,
            usedCount = satellites.count { it.usedInFix },
            totalCount = satellites.size,
            hdop = 1.5f,
            vdop = 2.0f,
            pdop = 2.5f
        )
    }
}

// 数据类
data class GnssStatusData(
    val satellites: List<SatelliteDetail>,
    val usedCount: Int,
    val totalCount: Int,
    val hdop: Float,
    val vdop: Float,
    val pdop: Float
)

data class SatelliteDetail(
    val prn: Int,
    val constellation: String,
    val snr: Float,
    val azimuth: Float,
    val elevation: Float,
    val usedInFix: Boolean
)
