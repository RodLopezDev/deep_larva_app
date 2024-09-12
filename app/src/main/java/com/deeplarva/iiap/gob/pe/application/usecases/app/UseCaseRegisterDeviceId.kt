package com.deeplarva.iiap.gob.pe.application.usecases.app

import com.deeplarva.iiap.gob.pe.domain.constants.SharedPreferencesConstants
import com.deeplarva.iiap.gob.pe.utils.PreferencesHelper
import java.util.UUID

class UseCaseRegisterDeviceId(private val preferences: PreferencesHelper) {
    fun execute() {
        val identifierExists = preferences.getString(SharedPreferencesConstants.DEVICE_ID)
        if(identifierExists == null) {
            preferences.saveString(SharedPreferencesConstants.DEVICE_ID, UUID.randomUUID().toString())
        }
    }
}