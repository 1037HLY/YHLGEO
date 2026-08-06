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
                timestamp = location.time,  // 直接使用毫秒时间戳
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
