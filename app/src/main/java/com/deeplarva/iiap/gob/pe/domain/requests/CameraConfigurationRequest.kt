package com.deeplarva.iiap.gob.pe.domain.requests

data class CameraConfigurationRequest (
    val deviceId: String,
    val brand: String,
    val model: String,
)