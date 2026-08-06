package com.geosurvey.toolbox.presentation

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

class LocationHelper(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var locationListener: LocationListener? = null
    private var onLocationUpdate: ((Location) -> Unit)? = null
    private var onSatelliteUpdate: ((Int) -> Unit)? = null
    private var isListening = false

    fun startLocationUpdates(
        onLocationUpdate: (Location) -> Unit,
        onSatelliteUpdate: (Int) -> Unit
    ) {
        this.onLocationUpdate = onLocationUpdate
        this.onSatelliteUpdate = onSatelliteUpdate

        if (isListening) {
            return
        }

        try {
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    onLocationUpdate(location)
                    updateSatelliteCount()
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                    // 状态变化，不需要处理
                }

                @Suppress("DEPRECATION")
                override fun onProviderEnabled(provider: String) {
                    // 提供者启用，不需要处理
                }

                @Suppress("DEPRECATION")
                override fun onProviderDisabled(provider: String) {
                    // 提供者禁用，不需要处理
                }
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                locationListener!!
            )
            isListening = true

            // 初始获取一次卫星数量
            updateSatelliteCount()

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
            isListening = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateSatelliteCount() {
        try {
            val gpsStatus = locationManager.getGpsStatus(null)
            var count = 0
            if (gpsStatus != null) {
                // 使用 Kotlin 的 for 循环遍历 Iterable
                for (sat in gpsStatus.satellites) {
                    count++
                }
            }
            onSatelliteUpdate?.invoke(count)
        } catch (e: SecurityException) {
            onSatelliteUpdate?.invoke(0)
        } catch (e: Exception) {
            onSatelliteUpdate?.invoke(0)
        }
    }
}
