package com.geosurvey.toolbox.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT MIN(timestamp) FROM track_points WHERE trackId = :trackId")
    suspend fun getStartTime(trackId: String): Date?

    @Query("SELECT MAX(timestamp) FROM track_points WHERE trackId = :trackId")
    suspend fun getEndTime(trackId: String): Date?

    @Query("DELETE FROM track_points WHERE trackId = :trackId")
    suspend fun deleteTrack(trackId: String)

    @Query("DELETE FROM track_points WHERE timestamp < :beforeDate")
    suspend fun deleteOldTracks(beforeDate: Date)
}
