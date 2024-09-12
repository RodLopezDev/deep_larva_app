package com.iiap.deeplarva.gob.pe.domain.response

data class AppConfigurationResponse (
    val version: String,
    val environment: AppEnvironment
)