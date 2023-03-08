package com.example.hpilitev3.data.db

data class SensorMessage(
    val hTime: String,
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