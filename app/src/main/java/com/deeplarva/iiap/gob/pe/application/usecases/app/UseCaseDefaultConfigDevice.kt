package com.deeplarva.iiap.gob.pe.application.usecases.app

import com.deeplarva.iiap.gob.pe.domain.constants.ConfigConstants
import com.deeplarva.iiap.gob.pe.utils.PreferencesHelper

class UseCaseDefaultConfigDevice(private val preferences: PreferencesHelper) {
    fun execute() {
        val isConfigPrev = preferences.getBoolean(ConfigConstants.CONFIG_FLAG_INITIAL_CONFIG, false)
        if (isConfigPrev){
            return
        }

        // ADD DEFAULT APP CONFIG
        preferences.saveBoolean(ConfigConstants.CONFIG_SHOW_SHUTTER_SPEED_CUSTOM, false)
        preferences.saveBoolean(ConfigConstants.CONFIG_SHOW_ISO_CUSTOM, false)
        preferences.saveBoolean(ConfigConstants.CONFIG_FLAG_INITIAL_CONFIG, true)
    }
}