package com.iiap.deeplarva.application.usecases.app

import com.iiap.deeplarva.domain.constants.CloudKeysConstants
import com.iiap.deeplarva.domain.constants.SharedPreferencesConstants
import com.iiap.deeplarva.domain.requests.CameraConfigurationRequest
import com.iiap.deeplarva.domain.response.CameraConfigurationResponse
import com.iiap.deeplarva.infraestructure.services.AppConfigurationServices
import com.iiap.deeplarva.modules.requests.RequestListener
import com.iiap.deeplarva.utils.PreferencesHelper

class UseCaseGetCameraConfiguration(
    private val brand: String,
    private val model: String,
    private val preferences: PreferencesHelper,
    private val services: AppConfigurationServices
) {
    fun execute(callback: () -> Unit): Boolean {
        val flag = preferences.getBoolean(CloudKeysConstants.FLAG_CAMERA_CONFIG, true)
        if (flag) {
            return false
        }

        preferences.saveBoolean(CloudKeysConstants.FLAG_CAMERA_CONFIG, true)

        val deviceId = preferences.getString(SharedPreferencesConstants.DEVICE_ID) ?: ""
        val request = CameraConfigurationRequest(deviceId, brand, model)
        services.getCameraConfiguration(request, object:
            RequestListener<CameraConfigurationResponse> {
            override fun onComplete(result: CameraConfigurationResponse) {
                preferences.saveBoolean(CloudKeysConstants.FLAG_CAMERA_CONFIG_EXIST, true)
                preferences.saveInt(CloudKeysConstants.ISO_VALUE, result.iso)
                preferences.saveLong(CloudKeysConstants.EXPOSURE_VALUE, result.exposure)
                preferences.saveLong(CloudKeysConstants.SHUTTER_SPEED_VALUE, result.shutterSpeed)
                callback()
            }
            override fun onFailure() {
                preferences.saveBoolean(CloudKeysConstants.FLAG_CAMERA_CONFIG_EXIST, false)
                callback()
            }
        })

        return true
    }
}