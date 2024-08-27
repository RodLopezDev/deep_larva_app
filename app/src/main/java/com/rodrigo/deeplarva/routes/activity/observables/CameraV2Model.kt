package com.rodrigo.deeplarva.routes.activity.observables

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.Duration

class CameraV2Model: ViewModel() {

    private val _iso = MutableLiveData<Int?>()
    private val _interval = MutableLiveData<Duration>()
    private val _previousShutterSpeed = MutableLiveData<Duration>()
    private val _shutterSpeed = MutableLiveData<Duration>()

    val iso: LiveData<Int?> = _iso
    val interval: LiveData<Duration?> = _interval
    val shutterSpeed: LiveData<Duration?> = _shutterSpeed
    val previousShutterSpeed: LiveData<Duration?> = _previousShutterSpeed

    fun setIso(newIso: Int?) {
        _iso.value = newIso
    }

    fun setInterval(newInterval: Duration) {
        _interval.value = newInterval
    }

    fun setShutterSpeed(newShutterSpeed: Duration) {
        _shutterSpeed.value = newShutterSpeed
    }

    fun setPreviousShutterSpeed(newPreviousShutterSpeed: Duration?) {
        _previousShutterSpeed.value = newPreviousShutterSpeed
    }
}