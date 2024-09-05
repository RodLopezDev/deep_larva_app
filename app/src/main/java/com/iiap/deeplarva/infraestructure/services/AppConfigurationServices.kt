package com.iiap.deeplarva.infraestructure.services

import com.iiap.deeplarva.domain.constants.AppConstants
import com.iiap.deeplarva.domain.response.AppConfigurationResponse
import com.iiap.deeplarva.modules.requests.RequestListener
import com.iiap.deeplarva.modules.requests.RequestManager

class AppConfigurationServices {
    fun getConfiguration(listener: RequestListener<AppConfigurationResponse>) {
        RequestManager.baseGet(
            AppConstants.SERVICE_BASE_URL,
            "x-api-key",
            AppConstants.SERVICE_API_KEY,
            listener
        )
    }
}