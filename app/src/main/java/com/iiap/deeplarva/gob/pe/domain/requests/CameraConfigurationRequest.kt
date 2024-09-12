package com.iiap.deeplarva.domain.requests

data class CameraConfigurationRequest (
    val deviceId: String,
    val brand: String,
    val model: String,
)