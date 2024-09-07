package com.iiap.deeplarva.infraestructure.services

import com.iiap.deeplarva.domain.constants.AppConstants
import com.iiap.deeplarva.domain.requests.AppConfigurationRequest
import com.iiap.deeplarva.domain.response.AppConfigurationResponse
import com.iiap.deeplarva.modules.requests.RequestListener
import com.iiap.deeplarva.modules.requests.RequestManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class AppConfigurationServices {
    fun getConfiguration(request: AppConfigurationRequest, listener: RequestListener<AppConfigurationResponse>) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter = moshi.adapter(AppConfigurationRequest::class.java)

        val json = jsonAdapter.toJson(request)

        RequestManager.basePost(
            AppConstants.SERVICE_BASE_URL,
            "x-api-key",
            AppConstants.SERVICE_API_KEY,
            json,
            listener
        )
    }
}