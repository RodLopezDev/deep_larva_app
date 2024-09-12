package com.deeplarva.iiap.gob.pe.application.usecases.cloud

import com.deeplarva.iiap.gob.pe.domain.constants.CloudKeysConstants
import com.deeplarva.iiap.gob.pe.domain.constants.SharedPreferencesConstants
import com.deeplarva.iiap.gob.pe.domain.requests.CameraConfigurationRequest
import com.deeplarva.iiap.gob.pe.domain.response.CameraConfigurationResponse
import com.deeplarva.iiap.gob.pe.infraestructure.services.AppConfigurationServices
import com.deeplarva.iiap.gob.pe.modules.requests.RequestListener
import com.deeplarva.iiap.gob.pe.utils.ExposureUtils
import com.deeplarva.iiap.gob.pe.utils.PreferencesHelper
import com.deeplarva.iiap.gob.pe.utils.SpeedUtils

class UseCaseGetCameraConfiguration(
    private val brand: String,
    private val model: String,
    private val preferences: PreferencesHelper,
    private val services: AppConfigurationServices
) {
    fun execute(callback: () -> Unit): Boolean {
        val flag = preferences.getBoolean(CloudKeysConstants.FLAG_CAMERA_CONFIG, false)
        if (flag) {
            return false
        }

        preferences.saveBoolean(CloudKeysConstants.FLAG_CAMERA_CONFIG, true)

        val deviceId = preferences.getString(SharedPreferencesConstants.DEVICE_ID) ?: ""
        val request = CameraConfigurationRequest(deviceId, brand, model)
        services.getCameraConfiguration(request, object:
            RequestListener<CameraConfigurationResponse> {
            override fun onComplete(result: CameraConfigurationResponse) {
                val formattedExpo = ExposureUtils.convertServerToValue(result.exposure)

                preferences.saveBoolean(CloudKeysConstants.FLAG_CAMERA_CONFIG_EXIST, true)
                preferences.saveInt(CloudKeysConstants.ISO_VALUE, result.iso)
                preferences.saveInt(CloudKeysConstants.EXPOSURE_VALUE,formattedExpo)
                preferences.saveInt(CloudKeysConstants.SHUTTER_SPEED_VALUE, result.shutterSpeed)

                preferences.saveInt(SharedPreferencesConstants.SENSITIVITY_VALUE, result.iso)
                preferences.saveInt(SharedPreferencesConstants.EXPOSURE_VALUE, formattedExpo)
                preferences.saveInt(SharedPreferencesConstants.SHUTTER_SPEED_TIME_VALUE, result.shutterSpeed)
                preferences.saveString(SharedPreferencesConstants.SHUTTER_SPEED_TIME_TEXT, SpeedUtils.shutterMlToString(result.shutterSpeed))
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