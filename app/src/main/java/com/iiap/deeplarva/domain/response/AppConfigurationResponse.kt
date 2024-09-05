package com.iiap.deeplarva.domain.response

data class AppConfigurationResponse (
    val version: String,
    val environment: AppEnvironment
)