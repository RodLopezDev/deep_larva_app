package com.rodrigo.deeplarva.routes.view

interface CameraActivityViewListener {
    fun onChangeExposure(exposure: Int)
    fun onChangeISO(exposure: Int)
    fun onChangeSpeed(exposure: Long)
    fun onCapture()
}