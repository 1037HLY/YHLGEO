package com.geosurvey.toolbox.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DrillSampleDao {
    @Insert
    suspend fun insert(sample: DrillSampleEntity)

    @Update
    suspend fun update(sample: DrillSampleEntity)

    @Query("SELECT * FROM drill_samples ORDER BY timestamp DESC")
    fun getAllSamples(): Flow<List<DrillSampleEntity>>

    @Query("SELECT * FROM drill_samples WHERE id = :sampleId")
    suspend fun getSample(sampleId: Long): DrillSampleEntity?

    @Query("DELETE FROM drill_samples WHERE id = :sampleId")
    suspend fun deleteSample(sampleId: Long)

    @Query("DELETE FROM drill_samples")
    suspend fun deleteAll()
}
