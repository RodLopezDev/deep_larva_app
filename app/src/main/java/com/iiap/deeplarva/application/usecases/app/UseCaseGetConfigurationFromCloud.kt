package com.iiap.deeplarva.application.usecases.app

import com.iiap.deeplarva.domain.constants.CloudKeysConstants
import com.iiap.deeplarva.domain.constants.SharedPreferencesConstants
import com.iiap.deeplarva.domain.requests.AppConfigurationRequest
import com.iiap.deeplarva.domain.response.AppConfigurationResponse
import com.iiap.deeplarva.infraestructure.services.AppConfigurationServices
import com.iiap.deeplarva.modules.requests.RequestListener
import com.iiap.deeplarva.utils.DateUtils
import com.iiap.deeplarva.utils.PreferencesHelper

class UseCaseGetConfigurationFromCloud(
    private val version: String,
    private val preferences: PreferencesHelper,
    private val services: AppConfigurationServices
) {
    fun execute(callback: () -> Unit): Boolean {
        val flag = preferences.getString(CloudKeysConstants.LAST_DATE_CHECKED, "") ?: ""
        if (flag != "" && DateUtils.isSameAsToday(flag)) {
            return false
        }

        val deviceId = preferences.getString(SharedPreferencesConstants.DEVICE_ID) ?: ""
        val request = AppConfigurationRequest(version, deviceId)
        services.getConfiguration(request, object:
            RequestListener<AppConfigurationResponse> {
            override fun onComplete(result: AppConfigurationResponse) {
                preferences.saveString(CloudKeysConstants.LAST_DATE_CHECKED, DateUtils.getToday())
                preferences.saveString(CloudKeysConstants.APP_VERSION, result.version)
                preferences.saveString(CloudKeysConstants.SERVER_URL, result.environment.API_SERVER_URL)
                preferences.saveString(CloudKeysConstants.SERVER_API_KEY, result.environment.API_SERVER_KEY)
                preferences.saveBoolean(CloudKeysConstants.ERROR_REQUEST, false)
                callback()
            }
            override fun onFailure() {
                preferences.saveBoolean(CloudKeysConstants.ERROR_REQUEST, true)
                callback()
            }
        })

        return true
    }
}