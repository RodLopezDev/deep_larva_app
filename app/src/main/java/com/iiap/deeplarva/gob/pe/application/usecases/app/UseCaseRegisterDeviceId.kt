package com.iiap.deeplarva.gob.pe.application.usecases.app

import com.iiap.deeplarva.gob.pe.domain.constants.SharedPreferencesConstants
import com.iiap.deeplarva.gob.pe.utils.PreferencesHelper
import java.util.UUID

class UseCaseRegisterDeviceId(private val preferences: PreferencesHelper) {
    fun execute() {
        val identifierExists = preferences.getString(SharedPreferencesConstants.DEVICE_ID)
        if(identifierExists == null) {
            preferences.saveString(SharedPreferencesConstants.DEVICE_ID, UUID.randomUUID().toString())
        }
    }
}