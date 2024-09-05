package com.iiap.deeplarva.infraestructure.services

import com.iiap.deeplarva.application.utils.Constants
import com.iiap.deeplarva.domain.response.AppConfigurationResponse
import com.iiap.deeplarva.modules.requests.RequestListener
import com.iiap.deeplarva.modules.requests.RequestManager

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