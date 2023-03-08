package com.example.hpilitev3.data.db

import android.util.Log
import androidx.room.Entity
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Entity(primaryKeys = ["hTime", "macAddress"])
data class Sensor(
    val hTime: LocalDateTime = LocalDateTime.now(),
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

class InvalidSensorException(message: String): Exception(message)

object DateTypeConverters {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")

    @TypeConverter
    @JvmStatic
    fun toLocalDateTime(value: String?): LocalDateTime {
        return formatter.parse(value, LocalDateTime::from)
    }

    @TypeConverter
    @JvmStatic
    fun fromLocalDateTime(date: LocalDateTime?): String? {
        return date?.format(formatter)
    }
}

fun Sensor.toSensorMessage(): SensorMessage {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
    return SensorMessage(
        hTime = this.hTime.format(formatter),
        cnt, gyroX, gyroY, gyroZ, accX, accY, accZ, magX, magY, magZ, roll, pitch, yaw, macAddress, petName
    )
}

/**
 * Create Table Datatable
 * (Htime datetime(3), Cnt int, Gyro_x float, Gyro_y float, Gyro_z float,
 * Acc_x float, Acc_y float, Acc_z float,
 * mag_x float, mag_y float, mag_z float,
 * Mac varchar(100),
 * PRIMARY KEY(Htime, Mac),
 * FOREIGN KEY(Mac) REFERENCES Device(Mac) on delete cascade);
 */