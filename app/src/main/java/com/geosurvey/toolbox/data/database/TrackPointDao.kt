package com.geosurvey.toolbox.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TrackPointDao {
    @Insert
    suspend fun insert(point: TrackPointEntity)

    @Query("SELECT * FROM track_points WHERE trackId = :trackId ORDER BY timestamp ASC")
    fun getTrackPoints(trackId: String): Flow<List<TrackPointEntity>>

    @Query("SELECT DISTINCT trackId FROM track_points ORDER BY timestamp DESC")
    fun getAllTrackIds(): Flow<List<String>>

    @Query("SELECT * FROM track_points WHERE trackId = :trackId ORDER BY timestamp ASC")
    suspend fun getTrackPointsSync(trackId: String): List<TrackPointEntity>

    @Query("SELECT COUNT(*) FROM track_points WHERE trackId = :trackId")
    suspend fun getPointCount(trackId: String): Int

    // 使用 Long 类型存储时间戳
    @Query("SELECT MIN(timestamp) FROM track_points WHERE trackId = :trackId")
    suspend fun getStartTimeMillis(trackId: String): Long?

    @Query("SELECT MAX(timestamp) FROM track_points WHERE trackId = :trackId")
    suspend fun getEndTimeMillis(trackId: String): Long?

    @Query("DELETE FROM track_points WHERE trackId = :trackId")
    suspend fun deleteTrack(trackId: String)

    @Query("DELETE FROM track_points WHERE timestamp < :beforeDateMillis")
    suspend fun deleteOldTracks(beforeDateMillis: Long)
}
