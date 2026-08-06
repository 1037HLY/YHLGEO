package com.geosurvey.toolbox.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SampleDao {
    @Insert
    suspend fun insert(sample: SampleEntity)

    @Update
    suspend fun update(sample: SampleEntity)

    @Query("SELECT * FROM samples ORDER BY timestamp DESC")
    fun getAllSamples(): Flow<List<SampleEntity>>

    @Query("SELECT * FROM samples WHERE id = :sampleId")
    suspend fun getSample(sampleId: Long): SampleEntity?

    @Query("DELETE FROM samples WHERE id = :sampleId")
    suspend fun deleteSample(sampleId: Long)

    @Query("DELETE FROM samples")
    suspend fun deleteAll()
}
