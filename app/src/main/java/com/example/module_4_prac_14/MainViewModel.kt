package com.example.module_4_prac_14

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val compassSensorManager = CompassSensorManager(application)

    val azimuth: StateFlow<Float> = compassSensorManager.azimuth

    val sensorAvailable: StateFlow<Boolean> = compassSensorManager.sensorAvailable

    fun startSensor() {
        compassSensorManager.startListening()
    }

    fun stopSensor() {
        compassSensorManager.stopListening()
    }

    override fun onCleared() {
        super.onCleared()
        compassSensorManager.stopListening()
    }
}
