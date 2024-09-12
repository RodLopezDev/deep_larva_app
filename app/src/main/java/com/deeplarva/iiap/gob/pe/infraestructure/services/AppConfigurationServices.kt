package com.deeplarva.iiap.gob.pe.infraestructure.services

import com.deeplarva.iiap.gob.pe.domain.constants.AppConstants
import com.deeplarva.iiap.gob.pe.domain.requests.AppConfigurationRequest
import com.deeplarva.iiap.gob.pe.domain.requests.CameraConfigurationRequest
import com.deeplarva.iiap.gob.pe.domain.response.AppConfigurationResponse
import com.deeplarva.iiap.gob.pe.domain.response.CameraConfigurationResponse
import com.deeplarva.iiap.gob.pe.modules.requests.RequestListener
import com.deeplarva.iiap.gob.pe.modules.requests.RequestManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class AppConfigurationServices {
    fun getConfiguration(request: AppConfigurationRequest, listener: RequestListener<AppConfigurationResponse>) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter = moshi.adapter(AppConfigurationRequest::class.java)

        val json = jsonAdapter.toJson(request)

        val url = "${AppConstants.SERVICE_BASE_URL}/api/v1/deep-larva"
        RequestManager.basePost(
            url,
            "x-api-key",
            AppConstants.SERVICE_API_KEY,
            json,
            listener
        )
    }
    fun getCameraConfiguration(request: CameraConfigurationRequest, listener: RequestListener<CameraConfigurationResponse>) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter = moshi.adapter(CameraConfigurationRequest::class.java)

        val json = jsonAdapter.toJson(request)

        val url = "${AppConstants.SERVICE_BASE_URL}/api/v2/deep-larva/camera-config"
        RequestManager.basePost(
            url,
            "x-api-key",
            AppConstants.SERVICE_API_KEY,
            json,
            listener
        )
    }
}