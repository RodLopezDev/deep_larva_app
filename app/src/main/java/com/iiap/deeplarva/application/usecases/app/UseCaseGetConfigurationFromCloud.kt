package com.iiap.deeplarva.application.usecases.app

import com.iiap.deeplarva.domain.constants.CloudKeysConstants
import com.iiap.deeplarva.domain.response.AppConfigurationResponse
import com.iiap.deeplarva.infraestructure.services.AppConfigurationServices
import com.iiap.deeplarva.modules.requests.RequestListener
import com.iiap.deeplarva.utils.DateUtils
import com.iiap.deeplarva.utils.PreferencesHelper

class UseCaseGetConfigurationFromCloud(
    private val preferences: PreferencesHelper,
    private val services: AppConfigurationServices
) {
    fun execute(callback: () -> Unit): Boolean {
        val flag = preferences.getString(CloudKeysConstants.LAST_DATE_CHECKED, "") ?: ""
        if (flag != "" && DateUtils.isSameAsToday(flag)) {
            return false
        }

        services.getConfiguration(object:
            RequestListener<AppConfigurationResponse> {
            override fun onComplete(result: AppConfigurationResponse) {
                preferences.saveString(CloudKeysConstants.LAST_DATE_CHECKED, DateUtils.getToday())
                preferences.saveString(CloudKeysConstants.APP_VERSION, result.version)
                preferences.saveString(CloudKeysConstants.SERVER_URL, result.environment.API_SERVER_URL)
                preferences.saveString(CloudKeysConstants.SERVER_API_KEY, result.environment.API_SERVER_KEY)
                callback()
            }
            override fun onFailure() {
                callback()
            }
        })

        return true
    }
}