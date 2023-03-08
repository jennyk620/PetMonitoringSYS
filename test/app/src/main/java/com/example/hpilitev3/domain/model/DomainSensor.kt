package com.example.hpilitev3.domain.model

import com.example.hpilitev3.data.db.Sensor
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Created by Jaehyeon on 2022/10/14.
 */
data class DomainSensor(
    val hTime: Long,
    val cnt: Int,
    val gyroX: Float,
    val gyroY: Float,
    val gyroZ: Float,
    val accX: Float,
    val accY: Float,
    val accZ: Float,
    val magX: Float,
    val magY: Float,
    val magZ: Float,
    val roll: Float,
    val pitch: Float,
    val yaw: Float,
    val macAddress: String,
    val petName: String,
)

fun DomainSensor.toSensor(): Sensor {

    //val dateTime = LocalDateTime.ofEpochSecond(this.hTime, 0, ZoneOffset.UTC)
    val dateTime = Instant.ofEpochMilli(this.hTime).atZone(ZoneId.systemDefault()).toLocalDateTime()
    return Sensor(
        gyroX = this.gyroX,
        gyroY = this.gyroY,
        gyroZ = this.gyroZ,
        accX = this.accX,
        accY = this.accY,
        accZ = this.accZ,
        magX = this.magX,
        magY = this.magY,
        magZ = this.magZ,
        roll = this.roll,
        pitch = this.pitch,
        yaw = this.yaw,
        macAddress = this.macAddress,
        cnt = this.cnt,
        petName = this.petName,
        hTime = dateTime
    )
}