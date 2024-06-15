package com.rodrigo.deeplarva.modules.camera.interfaces

interface CameraActivityViewListener {
    fun onChangeISO(exposure: Int)
    fun onChangeSpeed(exposure: Long)
    fun onCapture()
    fun onCloseView()
}