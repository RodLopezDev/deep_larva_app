package com.iiap.deeplarva.domain.response

data class CameraConfigurationResponse (
    val iso: Int,
    val exposure: Long,
    val shutterSpeed: Long,
)