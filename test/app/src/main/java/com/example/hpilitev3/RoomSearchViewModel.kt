package com.example.hpilitev3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hpilitev3.data.db.Sensor
import com.example.hpilitev3.data.db.SensorDao
import com.example.hpilitev3.data.repository.SensorRepositoryImpl
import com.example.hpilitev3.domain.repository.SensorRepository
import com.example.hpilitev3.domain.use_case.SensorUseCase
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Created by Jaehyeon on 2022/09/26.
 */
@HiltViewModel
class RoomSearchViewModel @Inject constructor(
    private val useCase: SensorUseCase
): ViewModel() {

    private val _data = MutableStateFlow<List<Sensor>>(emptyList())
    val data = _data.asStateFlow()

    fun getDatabaseData() {
        try {
            useCase.getSensors().onEach {
                _data.value = it.reversed()
            }.launchIn(viewModelScope)
        } catch (t: Throwable) {

        }
    }

}