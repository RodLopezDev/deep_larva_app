package com.rodrigo.deeplarva.routes.activity.view

interface ICameraV2ViewListener {
    // Behavior of View
    fun onTakePicture()
    fun onUpdateExposure(value: Int)

    // Functions for get values
    fun getMinExposure(): Int
    fun getMaxExposure(): Int
    fun getDefaultExposure(): Int
}