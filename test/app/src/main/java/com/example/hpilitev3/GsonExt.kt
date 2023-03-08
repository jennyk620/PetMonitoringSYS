package com.example.hpilitev3

import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val gson = Gson()
var FileName = ""

inline fun <reified T> String.fromJson() = gson.fromJson(this, T::class.java)

fun <T> T.toJson() = gson.toJson(this)

fun String.toFile(name: String, path: File?): File? {
    path ?: return null

    return try {
        val datePattern = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSSSSS")
        val local = LocalDateTime.now().format(datePattern)
        FileName = local + "_" + name + ".json"
        val file = File(path, FileName)

        if (!file.exists()) {
            file.createNewFile()
        }

        PrintWriter(FileWriter(file)).use {
            it.write(this)
        }

        file
    } catch (t: Throwable) {
        Log.e("String.toFile", "toFile: ${t.localizedMessage}", )
        null
    }
}

fun File.toMultipartBody(): MultipartBody.Part {
    return MultipartBody.Part.createFormData(
        name = "file",
        filename = FileName,
        body = this.asRequestBody("*/*".toMediaType())
    )
}