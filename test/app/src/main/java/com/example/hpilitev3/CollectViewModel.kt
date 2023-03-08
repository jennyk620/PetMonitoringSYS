package com.example.hpilitev3

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hpilitev3.data.db.Sensor
import com.example.hpilitev3.data.db.toSensorMessage
import com.example.hpilitev3.domain.model.DomainSensor
import com.example.hpilitev3.domain.use_case.SensorUseCase
import com.example.hpilitev3.presentation.event.CollectActivityEvent
import com.example.hpilitev3.CollectActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * Created by Jaehyeon on 2022/09/26.
 */
@HiltViewModel
class CollectViewModel @Inject constructor(
    private val useCase: SensorUseCase
) : ViewModel() {

    var path: File? = null
    var petName = ""
    var deviceAddress = ""

    private val _message = MutableLiveData("")
    val message: LiveData<String> get() = _message
    private val _response = MutableLiveData("")
    val response: LiveData<String> get() = _response
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading
    private val _state = MutableLiveData<Int>(999)
    val state: LiveData<Int> get() = _state
    private var deviceTime = -1
    private var first_get = -1
    private var lastTime = 0L
    var splitlist : List<Sensor>? = null
    var SENDCOUNT = 0
    var FINALCHECK = ""

    fun postData(): String {
        var state = ""
        var firstcol = 0
        var lastcol = 25000
        var Count = 0
        this.
        useCase.getSensors().onStart {
            _message.postValue("데이터 전송 중.")
//            _loading.postValue(true)
            _state.value = 0
        }.catch {
            Log.e("TAG", "getData: getData")
            _state.value = -1
        }.onEach {
            //5만 단위 씩 끊어 전송
            if(it.isEmpty()){
                _state.value = 1
            }
            else if(it.size > 25000){
                Log.e("size", it.size.toString())
                do{
                    Log.e("SENDCOUNT", SENDCOUNT.toString())
                    FINALCHECK = ""
                    SENDCOUNT++
                    if (lastcol <= it.size) {
                        splitlist = null
                        delay(1000)
                        splitlist = it.subList(firstcol, lastcol)
                        postSensorFile(splitlist!!)
                        Log.e("firstcol", firstcol.toString())
                        Log.e("lastcol", lastcol.toString())
                        firstcol = lastcol
                        lastcol += 25000
                    } else {
                        lastcol = it.size //- 1
                        splitlist = null
                        splitlist = it.subList(firstcol, lastcol)
                        FINALCHECK = "_FINAL"
                        Log.e("firstcol_F", firstcol.toString())
                        Log.e("lastcol_F", lastcol.toString())
                        postSensorFile(splitlist!!)
                        delay(1000)
                    }
                }while(FINALCHECK == "")
            }
            else {
                FINALCHECK = "_FINAL"
                postSensorFile(it)
            }
            Log.e("postData", it.size.toString())
        }.onCompletion {
//            _message.postValue("데이터 전송 완료.")
            _state.value = 1
            state = "ok"
            SENDCOUNT = 0
            Log.e("onCompletion", "onCompletion")
        }.launchIn(viewModelScope)
        return state
    }

    private fun postSensorFile(list: List<Sensor>) {
        val file = list.map { it.toSensorMessage() }.toJson().toFile(petName+"_"+SENDCOUNT+FINALCHECK, path)
        Log.e(javaClass.simpleName, "postSensorFile: File1", )
        if (file != null) {
            Log.e(javaClass.simpleName, "postSensorFile: File3", )
            Files.readAllLines(file.toPath(), Charsets.UTF_8).forEach(::println)
            useCase.postSensorFile(file.toMultipartBody())
                    .catch {
                        Log.e("TAG", "postData: Post Error")
                    }.onEach {
                            _response.postValue(it)
                        if(it == "success")
                            _state.value = 1
                        Log.e(javaClass.simpleName, "postSensorFile: File2", )
                    }.onCompletion{
//                        if(FINALCHECK != "_FINAL") {
//                            _state.value = -1
//                            _message.postValue("데이터 전송 완료.")
//                        }
//                        else
//                            _state.value = 1
//                        Log.e(javaClass.simpleName, "postSensorFile: onCompletion")
                    }.launchIn(viewModelScope)
        }
    }

//    private fun postSensor(list: List<Sensor>) {
//        useCase.postSensor(list.map { it.toSensorMessage() }.toJson().fromJson())
//            .catch {
//                Log.e("TAG", "postData: Post Error")
//            }.onEach {
//                _response.postValue(it)
//            }.onCompletion{
//                _message.postValue("데이터 전송 완료.")
////              _loading.postValue(false)
//                _state.value = 1
//            }.launchIn(viewModelScope)
//    }

    fun saveData(
        deviceTime: Int,
        acc: Array<String>,
        gyro: Array<String>,
        mag: Array<String>,
        rotate: Array<String>,
        petName: String
    ) {
        //Log.e("saveData", deviceTime.toString())
        //db 저장.
        if (first_get == -1) {
            this.deviceTime = deviceTime
            this.lastTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()?.toEpochMilli()?:0 //toEpochSecond(ZoneOffset.UTC)
            //Log.e("LASTTIME", this.lastTime.toString())
            first_get = 0
        }

        viewModelScope.launch {
            val sensor = DomainSensor(
                gyroX = gyro[0].toFloat(),
                gyroY = gyro[1].toFloat(),
                gyroZ = gyro[2].toFloat(),
                accX = acc[0].toFloat(),
                accY = acc[1].toFloat(),
                accZ = acc[2].toFloat(),
                magX = mag[0].toFloat(),
                magY = mag[1].toFloat(),
                magZ = mag[2].toFloat(),
                roll = rotate[0].toFloat(),
                pitch = rotate[1].toFloat(),
                yaw = rotate[2].toFloat(),
                macAddress = deviceAddress,
                cnt = deviceTime,
                petName = petName,
                hTime = (deviceTime - this@CollectViewModel.deviceTime) * 20 + lastTime
            )
            useCase.insertSensor(
                sensor
            )
        }
    }
}