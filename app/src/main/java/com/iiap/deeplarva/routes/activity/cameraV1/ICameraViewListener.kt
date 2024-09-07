package com.iiap.deeplarva.routes.activity.cameraV1

interface ICameraViewListener {
    // Behavior of View
    fun onTakePicture()
    fun onClose()
    fun onUpdateExposure(value: Int)
    fun onUpdateSensitivitySensor(value: Int)
    fun onUpdateShootSpeed(value: Int)
}