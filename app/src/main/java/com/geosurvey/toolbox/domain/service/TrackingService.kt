package com.geosurvey.toolbox.domain.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.geosurvey.toolbox.R
import com.geosurvey.toolbox.data.database.AppDatabase
import com.geosurvey.toolbox.data.database.TrackPointEntity
import com.geosurvey.toolbox.presentation.GnssStatusData
import com.geosurvey.toolbox.presentation.LocationHelper
import kotlinx.coroutines.*

class TrackingService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationHelper: LocationHelper
    private lateinit var database: AppDatabase
    private var currentTrackId: String? = null
    private var isRecording = false
    private var pointCount = 0

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "tracking_channel"
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_GET_STATUS = "ACTION_GET_STATUS"
        const val EXTRA_TRACK_ID = "EXTRA_TRACK_ID"

        fun startService(context: Context, trackId: String) {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TRACK_ID, trackId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationHelper = LocationHelper(this)
        database = AppDatabase.getDatabase(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val trackId = intent.getStringExtra(EXTRA_TRACK_ID) ?: java.util.UUID.randomUUID().toString()
                startTracking(trackId)
            }
            ACTION_STOP -> {
                stopTracking()
            }
            ACTION_GET_STATUS -> {
                broadcastStatus()
            }
        }
        return START_STICKY
    }

    private fun startTracking(trackId: String) {
        if (isRecording) return

        currentTrackId = trackId
        isRecording = true
        pointCount = 0

        startForeground(NOTIFICATION_ID, createNotification("正在记录轨迹...", 0))

        locationHelper.startLocationUpdates(
            onLocationUpdate = { location ->
                saveLocation(trackId, location)
            },
            onGnssStatusUpdate = { statusData ->
                updateNotification(statusData)
            }
        )

        broadcastStatus()
    }

    private fun stopTracking() {
        isRecording = false
        locationHelper.stopLocationUpdates()
        stopForeground(true)
        stopSelf()
        broadcastStatus()
    }

    private fun saveLocation(trackId: String, location: Location) {
        serviceScope.launch {
            try {
                val entity = TrackPointEntity(
                    trackId = trackId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = location.altitude,
                    speed = location.speed,
                    bearing = location.bearing,
                    accuracy = location.accuracy,
                    timestamp = location.time,
                    provider = location.provider ?: "gps",
                    satelliteCount = 0,
                    hdop = 0f,
                    pdop = 0f
                )
                database.trackPointDao().insert(entity)
                pointCount++

                if (pointCount % 10 == 0) {
                    updateNotification("已记录 $pointCount 个点", pointCount)
                    broadcastStatus()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateNotification(statusData: GnssStatusData?) {
        val text = if (statusData != null) {
            "卫星: ${statusData.totalCount}  |  已记录: $pointCount 点"
        } else {
            "已记录: $pointCount 点"
        }
        val notification = createNotification(text, pointCount)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun updateNotification(text: String, count: Int) {
        val notification = createNotification(text, count)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(text: String, count: Int): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("📍 轨迹记录中")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)

        if (count > 0) {
            builder.setProgress(100, (count % 100), false)
        }

        val stopIntent = Intent(this, TrackingService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            "停止",
            pendingIntent
        )

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "轨迹记录服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于后台持续记录GPS轨迹"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun broadcastStatus() {
        val intent = Intent("com.geosurvey.toolbox.TRACKING_STATUS").apply {
            putExtra("isRecording", isRecording)
            putExtra("trackId", currentTrackId ?: "")
            putExtra("pointCount", pointCount)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
