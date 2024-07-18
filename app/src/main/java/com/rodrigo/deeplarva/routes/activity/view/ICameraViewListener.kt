package com.rodrigo.deeplarva.routes.activity.view

interface ICameraViewListener {
    // Behavior of View
    fun onTakePicture()
    fun onClose()
    fun onUpdateExposure(value: Int)

    // Functions for get values
    fun getMinExposure(): Int
    fun getMaxExposure(): Int
    fun getDefaultExposure(): Int
}