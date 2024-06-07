package com.rodrigo.deeplarva.routes.camera.interfaces

interface CameraActivityViewListener {
    fun onChangeExposure(exposure: Int)
    fun onChangeISO(exposure: Int)
    fun onChangeSpeed(exposure: Long)
    fun onCapture()
    fun onCloseView()
}