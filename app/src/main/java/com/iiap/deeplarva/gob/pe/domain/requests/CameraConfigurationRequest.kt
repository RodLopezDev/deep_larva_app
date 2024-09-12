package com.iiap.deeplarva.gob.pe.domain.requests

data class CameraConfigurationRequest (
    val deviceId: String,
    val brand: String,
    val model: String,
)