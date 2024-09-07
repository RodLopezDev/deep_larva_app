package com.iiap.deeplarva.domain.requests

data class AppConfigurationRequest (
    val version: String,
    val deviceId: String,
)