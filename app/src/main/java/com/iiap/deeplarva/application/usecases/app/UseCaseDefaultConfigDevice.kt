package com.iiap.deeplarva.application.usecases.app

import com.iiap.deeplarva.domain.constants.SharedPreferencesConstants
import com.iiap.deeplarva.utils.PreferencesHelper

class UseCaseDefaultConfigDevice(private val preferences: PreferencesHelper) {
    fun execute() {
        val isConfigPrev = preferences.getBoolean(SharedPreferencesConstants.CONFIG_FLAG_INITIAL_CONFIG, false)
        if (isConfigPrev){
            return
        }

        // ADD DEFAULT APP CONFIG
        preferences.saveBoolean(SharedPreferencesConstants.CONFIG_FLAG_INITIAL_CONFIG, true)
    }
}