package com.rodrigo.deeplarva.infraestructure.services

import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.domain.response.AppConfigurationResponse
import com.rodrigo.deeplarva.modules.requests.RequestListener
import com.rodrigo.deeplarva.modules.requests.RequestManager

class AppConfigurationServices {
    fun getConfiguration(listener: RequestListener<AppConfigurationResponse>) {
        RequestManager.baseGet(
            Constants.SERVICE_BASE_URL,
            "x-api-key",
            Constants.SERVICE_API_KEY,
            listener
        )
    }
}