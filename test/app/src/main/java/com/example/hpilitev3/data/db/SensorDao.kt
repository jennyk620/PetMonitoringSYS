package com.example.hpilitev3.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Created by Jaehyeon on 2022/09/26.
 */
@Dao
interface SensorDao {

    @Query("SELECT * FROM sensor")
    fun getSensor(): Flow<List<Sensor>>

    @Query("SELECT * FROM sensor")
    fun getSensors(): List<Sensor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensor(sensor: Sensor)

}