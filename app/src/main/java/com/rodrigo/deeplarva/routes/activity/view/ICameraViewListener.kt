package com.rodrigo.deeplarva.routes.activity.view

interface ICameraViewListener {
    // Behavior of View
    fun onTakePicture()
    fun onUpdateExposure(value: Int)
    fun onClose()

    // Functions for get values
    fun getMinExposure(): Int
    fun getMaxExposure(): Int
    fun getDefaultExposure(): Int
}