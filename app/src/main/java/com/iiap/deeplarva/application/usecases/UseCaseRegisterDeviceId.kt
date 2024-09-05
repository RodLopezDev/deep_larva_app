package com.iiap.deeplarva.application.usecases

import android.content.Context
import com.iiap.deeplarva.application.utils.Constants
import com.iiap.deeplarva.helpers.PreferencesHelper
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