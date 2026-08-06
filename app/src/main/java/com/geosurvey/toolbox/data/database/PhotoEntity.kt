package com.geosurvey.toolbox.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val timestamp: Long,
    val strike: Float? = null,
    val dip: Float? = null,
    val dipDirection: Float? = null,
    val note: String? = null,
    val watermarkText: String? = null
)
