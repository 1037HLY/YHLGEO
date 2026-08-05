package com.geosurvey.toolbox

import android.app.Application
import androidx.room.Room
import com.geosurvey.toolbox.data.database.AppDatabase

class GeoSurveyApplication : Application() {

    companion object {
        lateinit var instance: GeoSurveyApplication
            private set
    }

    // Database instance (占位，后续实现)
    val database by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "geo_survey_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
