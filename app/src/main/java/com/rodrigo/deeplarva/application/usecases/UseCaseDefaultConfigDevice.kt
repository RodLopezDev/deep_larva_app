package com.rodrigo.deeplarva.application.usecases

import android.content.Context
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.helpers.PreferencesHelper

class UseCaseDefaultConfigDevice(private val context: Context) {
    fun execute() {
        val helper = PreferencesHelper(context)
        val isConfigPrev = helper.getBoolean(Constants.CONFIG_SHARED_PREFERENCES_FLAG_INITIAL_CONFIG, false)
        if (isConfigPrev){
            return
        }

        helper.saveBoolean(Constants.CONFIG_SHARED_PREFERENCES_CAMERA_ACTIVITY_V2, true)
        helper.saveBoolean(Constants.CONFIG_SHARED_PREFERENCES_FLAG_INITIAL_CONFIG, true)
    }
}