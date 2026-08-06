package com.geosurvey.toolbox.presentation

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kotlinx.coroutines.*

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
            // 使用现代的LocationListener实现
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    onLocationUpdate(location)
                    updateSatelliteCount()
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                    // 状态变化，不需要处理
                }

                // 注意：Android 30+ 中这两个方法已被废弃，但为了兼容性保留
                @Suppress("DEPRECATION")
                override fun onProviderEnabled(provider: String) {
                    // 提供者启用，不需要处理
                }

                @Suppress("DEPRECATION")
                override fun onProviderDisabled(provider: String) {
                    // 提供者禁用，不需要处理
                }
            }

            // 使用GPS Provider
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L, // 1秒更新一次
                1f,    // 移动1米更新
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
            val count = gpsStatus?.satellites?.count() ?: 0
            onSatelliteUpdate?.invoke(count)
        } catch (e: SecurityException) {
            // 权限问题
        } catch (e: Exception) {
            // 其他异常
        }
    }
}
