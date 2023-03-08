package com.example.hpilitev3.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Created by Jaehyeon on 2022/09/26.
 */
@Database(
    entities = [Sensor::class],
    version = 1
)
@TypeConverters(DateTypeConverters::class)
abstract class SensorDatabase: RoomDatabase() {

    abstract val sensorDao: SensorDao

    companion object {
        const val DATABASE_NAME = "sensor"
    }
}