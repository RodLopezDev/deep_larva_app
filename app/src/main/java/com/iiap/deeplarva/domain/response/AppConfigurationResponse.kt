package com.iiap.deeplarva.domain.response

data class AppConfigurationResponse (
    val version: String,
    val environment: Environment
)

data class Environment (
    val API_SERVER_URL: String,
    val  API_SERVER_KEY: String
)