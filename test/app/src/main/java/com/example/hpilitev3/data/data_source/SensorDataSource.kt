package com.example.hpilitev3.data.data_source

import android.util.Log
import com.example.hpilitev3.data.model.Response
import com.example.hpilitev3.data.remote.SensorApi
import com.google.gson.JsonArray
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * Created by Jaehyeon on 2022/10/11.
 */
class SensorDataSource @Inject constructor(
    private val service: SensorApi
) {

    suspend fun postSensor(json: JsonArray): Response =
        service.addSensor(json)

    suspend fun postSensor(file: MultipartBody.Part): Response {
        Log.e(javaClass.simpleName, "postSensorFile: SensorDataSource", )
        return service.postSensorDbFile(file)
    }
}