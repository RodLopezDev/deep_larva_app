package com.iiap.deeplarva.application.usecases.app

import android.content.Context
import com.iiap.deeplarva.domain.constants.SharedPreferencesConstants
import com.iiap.deeplarva.utils.PreferencesHelper

class UseCaseDefaultConfigDevice(private val context: Context) {
    fun execute() {
        val helper = PreferencesHelper(context)
        val isConfigPrev = helper.getBoolean(SharedPreferencesConstants.CONFIG_FLAG_INITIAL_CONFIG, false)
        if (isConfigPrev){
            return
        }

        helper.saveBoolean(SharedPreferencesConstants.CONFIG_CAMERA_ACTIVITY_V2, true)
        helper.saveBoolean(SharedPreferencesConstants.CONFIG_FLAG_INITIAL_CONFIG, true)
    }
}