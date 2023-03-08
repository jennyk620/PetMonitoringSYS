package com.example.hpilitev3.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.hpilitev3.data.data_source.SensorDataSource
import com.example.hpilitev3.data.db.Sensor
import com.example.hpilitev3.data.db.SensorDatabase
import com.example.hpilitev3.data.model.Response
import com.example.hpilitev3.domain.pagin_source.SensorPagingSource
import com.example.hpilitev3.domain.repository.SensorRepository
import com.google.gson.JsonArray
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * Created by Jaehyeon on 2022/10/10.
 */
class SensorRepositoryImpl @Inject constructor(
    private val db: SensorDatabase,
    private val dataSource: SensorDataSource
): SensorRepository {

    override fun getSensors(): Flow<List<Sensor>> {
        return db.sensorDao.getSensor()
    }

    override fun getSensors2(): Flow<PagingData<Sensor>> {
        return Pager(
            config = PagingConfig(pageSize = SensorPagingSource.PAGING_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { SensorPagingSource(db.sensorDao) }
        ).flow
    }

    override suspend fun insertSensor(sensor: Sensor) {
        db.sensorDao.insertSensor(sensor)
    }

    override suspend fun postSensor(json: JsonArray): Response {
        return dataSource.postSensor(json)
    }

    override suspend fun postSensor(file: MultipartBody.Part): Response {
        Log.e(javaClass.simpleName, "postSensorFile: repository", )
        return dataSource.postSensor(file)
    }

}