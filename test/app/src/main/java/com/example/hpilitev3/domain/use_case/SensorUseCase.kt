package com.example.hpilitev3.domain.use_case

import android.util.Log
import androidx.paging.PagingData
import com.example.hpilitev3.data.db.Sensor
import com.example.hpilitev3.data.db.SensorDao
import com.example.hpilitev3.domain.model.DomainSensor
import com.example.hpilitev3.domain.model.toSensor
import com.example.hpilitev3.domain.repository.SensorRepository
import com.google.gson.JsonArray
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * Created by Jaehyeon on 2022/10/10.
 */
class SensorUseCase @Inject constructor(
    private val repository: SensorRepository
) {

    fun getSensors(): Flow<List<Sensor>> = repository.getSensors()

    fun getSensors2(): Flow<PagingData<Sensor>> = repository.getSensors2()

    suspend fun insertSensor(sensor: DomainSensor) {
        repository.insertSensor(sensor.toSensor())
    }

    fun postSensor(json: JsonArray): Flow<String> = flow {
        emit(repository.postSensor(json).message)
    }

    fun postSensorFile(file: MultipartBody.Part): Flow<String> = flow {
        Log.e(javaClass.simpleName, "postSensorFile: usecase", )
        emit(repository.postSensor(file).message)
    }

}