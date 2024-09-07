package com.iiap.deeplarva.application.usecases.app

import android.content.Context
import com.iiap.deeplarva.domain.constants.CloudKeysConstants
import com.iiap.deeplarva.domain.response.AppConfigurationResponse
import com.iiap.deeplarva.infraestructure.services.AppConfigurationServices
import com.iiap.deeplarva.modules.requests.RequestListener
import com.iiap.deeplarva.utils.DateUtils
import com.iiap.deeplarva.utils.PreferencesHelper

class UseCaseGetConfigurationFromCloud(
    private val context: Context
) {
    fun execute(callback: () -> Unit): Boolean {
        val helper = PreferencesHelper(context)
        val flag = helper.getString(CloudKeysConstants.LAST_DATE_CHECKED, "") ?: ""
        if (flag != "" && DateUtils.isSameAsToday(flag)) {
            return false
        }

        AppConfigurationServices().getConfiguration(object:
            RequestListener<AppConfigurationResponse> {
            override fun onComplete(result: AppConfigurationResponse) {
                helper.saveString(CloudKeysConstants.LAST_DATE_CHECKED, DateUtils.getToday())
                helper.saveString(CloudKeysConstants.APP_VERSION, result.version)
                helper.saveString(CloudKeysConstants.SERVER_URL, result.environment.API_SERVER_URL)
                helper.saveString(CloudKeysConstants.SERVER_API_KEY, result.environment.API_SERVER_KEY)
                callback()
            }
            override fun onFailure() {
                callback()
            }
        })

        return true
    }
}