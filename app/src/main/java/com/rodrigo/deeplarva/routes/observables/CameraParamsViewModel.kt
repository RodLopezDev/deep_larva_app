package com.rodrigo.deeplarva.routes.observables

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraParamsViewModel(): ViewModel() {
    private val _ev = MutableLiveData<Int>().apply {
        value = 0
    }
    private val _iso = MutableLiveData<Int>().apply {
        value = 0
    }
    private val _speed = MutableLiveData<Long>().apply {
        value = 0
    }

    val ev: LiveData<Int?> = _ev
    val iso: LiveData<Int?> = _iso
    val speed: LiveData<Long?> = _speed

    fun updateEV(ev: Int) {
        _ev.value = ev
    }
    fun updateISO(iso: Int) {
        _iso.value = iso
    }
    fun updateSpeed(speed: Long) {
        _speed.value = speed
    }
}