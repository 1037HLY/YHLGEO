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
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.concurrent.TimeUnit

class LocationHelper(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var gnssStatusListener: GnssStatus.Callback? = null
    private var onLocationUpdate: ((Location) -> Unit)? = null
    private var onGnssStatusUpdate: ((GnssStatusData) -> Unit)? = null
    private var isListening = false
    private var isFusedListening = false
    private var nativeListener: LocationListener? = null
    private var lastLocationTime = 0L

    init {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        } catch (e: Exception) {
            fusedLocationClient = null
        }
    }

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

        if (!hasLocationPermission()) {
            return
        }

        try {
            // 1. 尝试FusedLocationProviderClient
            if (fusedLocationClient != null) {
                try {
                    startFusedLocationUpdates()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 2. 原生GPS监听（兼容所有设备）
            try {
                startNativeGpsUpdates()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 3. GNSS状态监听（Android 7.0+）
            try {
                startGnssStatusUpdates()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            isListening = true

            // 4. 发送缓存位置
            sendLastKnownLocation()

            // 5. 如果30秒内没有位置更新，强制刷新
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                if (lastLocationTime == 0L || System.currentTimeMillis() - lastLocationTime > 30000) {
                    forceLocationRefresh()
                }
            }, 30000)

        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startFusedLocationUpdates() {
        if (isFusedListening) return
        if (!hasLocationPermission()) return

        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000L
            ).apply {
                setMinUpdateIntervalMillis(500L)
                setMinUpdateDistanceMeters(0.5f)
                setWaitForAccurateLocation(false)
            }.build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.locations?.let { locations ->
                        locations.lastOrNull()?.let { location ->
                            lastLocationTime = System.currentTimeMillis()
                            onLocationUpdate?.invoke(location)
                        }
                    }
                }
            }

            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            isFusedListening = true
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startNativeGpsUpdates() {
        if (!hasLocationPermission()) return

        try {
            nativeListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    lastLocationTime = System.currentTimeMillis()
                    onLocationUpdate?.invoke(location)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

                @Suppress("DEPRECATION")
                override fun onProviderEnabled(provider: String) {}

                @Suppress("DEPRECATION")
                override fun onProviderDisabled(provider: String) {}
            }

            // 尝试GPS Provider
            try {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        500L,
                        0.5f,
                        nativeListener!!
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 尝试Network Provider（备选）
            try {
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000L,
                        5f,
                        nativeListener!!
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 尝试Passive Provider（被动定位）
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER,
                    2000L,
                    10f,
                    nativeListener!!
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun forceLocationRefresh() {
        if (!hasLocationPermission()) return
        try {
            // 强制请求一次位置更新
            val forceListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    lastLocationTime = System.currentTimeMillis()
                    onLocationUpdate?.invoke(location)
                    try {
                        locationManager.removeUpdates(this)
                    } catch (e: Exception) {
                        // 忽略
                    }
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                @Suppress("DEPRECATION")
                override fun onProviderEnabled(provider: String) {}
                @Suppress("DEPRECATION")
                override fun onProviderDisabled(provider: String) {}
            }

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0L,
                    0f,
                    forceListener
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startGnssStatusUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                gnssStatusListener = object : GnssStatus.Callback() {
                    override fun onSatelliteStatusChanged(status: GnssStatus) {
                        val data = parseGnssStatus(status)
                        onGnssStatusUpdate?.invoke(data)
                    }
                }
                locationManager.registerGnssStatusCallback(gnssStatusListener!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            @Suppress("DEPRECATION")
            try {
                locationManager.addGpsStatusListener { event ->
                    if (event == 1) {
                        @Suppress("DEPRECATION")
                        val gpsStatus = locationManager.getGpsStatus(null)
                        if (gpsStatus != null) {
                            val data = parseGpsStatus(gpsStatus)
                            onGnssStatusUpdate?.invoke(data)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendLastKnownLocation() {
        if (!hasLocationPermission()) return

        try {
            // 从GPS获取
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let {
                    onLocationUpdate?.invoke(it)
                    return
                }
            }

            // 从网络获取
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let {
                    onLocationUpdate?.invoke(it)
                    return
                }
            }

            // 从FusedLocation获取
            if (fusedLocationClient != null && hasLocationPermission()) {
                fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                    location?.let {
                        onLocationUpdate?.invoke(it)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopLocationUpdates() {
        try {
            if (isFusedListening) {
                locationCallback?.let {
                    fusedLocationClient?.removeLocationUpdates(it)
                }
                isFusedListening = false
            }

            nativeListener?.let {
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
