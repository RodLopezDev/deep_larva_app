package com.deeplarva.iiap.gob.pe.domain.response

data class CameraConfigurationResponse (
    val iso: Int,
    val exposure: Float,
    val shutterSpeed: Int,
)