package com.example.hpilitev3.domain.repository

import androidx.paging.PagingData
import com.example.hpilitev3.data.db.Sensor
import com.example.hpilitev3.data.db.SensorDao
import com.example.hpilitev3.data.model.Response
import com.google.gson.JsonArray
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

/**
 * Created by Jaehyeon on 2022/10/10.
 */
interface SensorRepository {

    fun getSensors(): Flow<List<Sensor>>
    fun getSensors2(): Flow<PagingData<Sensor>>
    suspend fun insertSensor(sensor: Sensor)
    suspend fun postSensor(json: JsonArray): Response
    suspend fun postSensor(file: MultipartBody.Part): Response

}