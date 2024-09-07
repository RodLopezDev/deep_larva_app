package com.iiap.deeplarva.application.usecases.app

import android.content.Context
import com.iiap.deeplarva.domain.constants.SharedPreferencesConstants
import com.iiap.deeplarva.utils.PreferencesHelper
import java.util.UUID

class UseCaseRegisterDeviceId(private val context: Context) {
    fun execute() {
        val helper = PreferencesHelper(context)
        val identifierExists = helper.getString(SharedPreferencesConstants.DEVICE_ID)
        if(identifierExists == null) {
            helper.saveString(SharedPreferencesConstants.DEVICE_ID, UUID.randomUUID().toString())
        }
    }
}