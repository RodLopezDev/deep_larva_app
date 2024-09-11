package com.iiap.deeplarva.domain.response

data class CameraConfigurationResponse (
    val iso: Int,
    val exposure: Float,
    val shutterSpeed: Long,
)