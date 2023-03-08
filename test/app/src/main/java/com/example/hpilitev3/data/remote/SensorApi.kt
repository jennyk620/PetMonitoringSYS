package com.example.hpilitev3.data.remote

import com.example.hpilitev3.data.model.Response
import com.google.gson.JsonArray
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Created by Jaehyeon on 2022/10/11.
 */
interface SensorApi {

    @POST("/sensor")
    suspend fun addSensor(
        @Body
        json: JsonArray
    ): Response

    @Multipart
    @POST("/sensor_send")
    suspend fun postSensorDbFile(
        @Part file: MultipartBody.Part
    ): Response

}