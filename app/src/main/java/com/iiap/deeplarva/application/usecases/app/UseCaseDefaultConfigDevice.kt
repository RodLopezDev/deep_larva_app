package com.iiap.deeplarva.application.usecases.app

import com.iiap.deeplarva.domain.constants.ConfigConstants
import com.iiap.deeplarva.utils.PreferencesHelper

class UseCaseDefaultConfigDevice(private val preferences: PreferencesHelper) {
    fun execute() {
        val isConfigPrev = preferences.getBoolean(ConfigConstants.CONFIG_FLAG_INITIAL_CONFIG, false)
        if (isConfigPrev){
            return
        }

        // ADD DEFAULT APP CONFIG
        preferences.saveBoolean(ConfigConstants.CONFIG_SHOW_SHUTTER_SPEED_CUSTOM, false)
        preferences.saveBoolean(ConfigConstants.CONFIG_FLAG_INITIAL_CONFIG, true)
    }
}