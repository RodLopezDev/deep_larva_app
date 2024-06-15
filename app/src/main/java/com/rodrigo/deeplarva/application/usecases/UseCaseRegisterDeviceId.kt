package com.rodrigo.deeplarva.application.usecases

import android.content.Context
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.utils.PreferencesHelper
import java.util.UUID

class UseCaseRegisterDeviceId(private val context: Context) {
    fun execute() {
        val helper = PreferencesHelper(context)
        val identifierExists = helper.getString(Constants.SHARED_PREFERENCES_DEVICE_ID)
        if(identifierExists == null) {
            helper.saveString(Constants.SHARED_PREFERENCES_DEVICE_ID, UUID.randomUUID().toString())
        }
    }
}