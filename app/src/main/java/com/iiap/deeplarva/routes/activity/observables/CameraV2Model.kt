package com.rodrigo.deeplarva.routes.activity.observables

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.Duration

class CameraV2Model: ViewModel() {

    private val _iso = MutableLiveData<Int?>()
    private val _interval = MutableLiveData<Duration?>()
    private val _exposure = MutableLiveData<Int?>()
    private val _previousShutterSpeed = MutableLiveData<Int?>()
    private val _shutterSpeed = MutableLiveData<Int?>()

    val iso: LiveData<Int?> = _iso
    val exposure: LiveData<Int?> = _exposure
    val interval: LiveData<Duration?> = _interval
    val shutterSpeed: LiveData<Int?> = _shutterSpeed
    val previousShutterSpeed: LiveData<Int?> = _previousShutterSpeed

    fun setIso(newIso: Int?) {
        _iso.value = newIso
    }

    fun setExposure(exposure: Int?) {
        _exposure.value = exposure
    }

    fun setInterval(newInterval: Duration?) {
        _interval.value = newInterval
    }

    fun setShutterSpeed(newShutterSpeed: Int) {
        _shutterSpeed.value = newShutterSpeed
    }

    fun setPreviousShutterSpeed(newPreviousShutterSpeed: Int?) {
        _previousShutterSpeed.value = newPreviousShutterSpeed
    }
}