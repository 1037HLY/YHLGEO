package com.geosurvey.toolbox.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttitudeDao {
    @Insert
    suspend fun insert(attitude: AttitudeEntity)

    @Query("SELECT * FROM attitudes ORDER BY timestamp DESC")
    fun getAllAttitudes(): Flow<List<AttitudeEntity>>

    @Query("SELECT * FROM attitudes WHERE id = :attitudeId")
    suspend fun getAttitude(attitudeId: Long): AttitudeEntity?

    @Query("DELETE FROM attitudes WHERE id = :attitudeId")
    suspend fun deleteAttitude(attitudeId: Long)

    @Query("SELECT * FROM attitudes WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getAttitudesBetween(startTime: Long, endTime: Long): List<AttitudeEntity>
}
