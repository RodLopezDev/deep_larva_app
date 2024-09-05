package com.iiap.deeplarva.routes.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.iiap.deeplarva.R
import com.iiap.deeplarva.application.usecases.UseCaseDefaultConfigDevice
import com.iiap.deeplarva.application.usecases.UseCaseRegisterDeviceId
import com.iiap.deeplarva.domain.constants.CloudKeysConstants
import com.iiap.deeplarva.domain.constants.PermissionsConstans
import com.iiap.deeplarva.domain.response.AppConfigurationResponse
import com.iiap.deeplarva.helpers.PreferencesHelper
import com.iiap.deeplarva.infraestructure.services.AppConfigurationServices
import com.iiap.deeplarva.modules.requests.RequestListener
import com.iiap.deeplarva.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity: AppCompatActivity() {
    private val splashScreenTime: Long = 2000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spash_screen)

        UseCaseRegisterDeviceId(this).execute()
        UseCaseDefaultConfigDevice(this).execute()

        val requiredCloud = getCloudConfiguration()
        if(requiredCloud) {
            return
        }

        GlobalScope.launch {
            delay(splashScreenTime)
            withContext(Dispatchers.Main) {
                launchActivity()
            }
        }
    }
    private fun getCloudConfiguration(): Boolean {
        val helper = PreferencesHelper(this)
        val flag = helper.getString(CloudKeysConstants.LAST_DATE_CHECKED, "") ?: ""
        if (flag != "" && DateUtils.isSameAsToday(flag)) {
            return false
        }

        AppConfigurationServices().getConfiguration(object: RequestListener<AppConfigurationResponse> {
            override fun onComplete(result: AppConfigurationResponse) {
                helper.saveString(CloudKeysConstants.LAST_DATE_CHECKED, DateUtils.getToday())
                helper.saveString(CloudKeysConstants.APP_VERSION, result.version)
                helper.saveString(CloudKeysConstants.SERVER_URL, result.environment.API_SERVER_URL)
                helper.saveString(CloudKeysConstants.SERVER_API_KEY, result.environment.API_SERVER_KEY)
                launchActivity()
            }
            override fun onFailure() {
                launchActivity()
            }
        })

        return true
    }

    private fun launchActivity() {
        var intent = Intent(this@SplashActivity,
            if (grantedPermissions()) PicturesActivity::class.java else PermissionsHandlerActivity::class.java
        )
        startActivity(intent)
        finish()
    }

    private fun grantedPermissions (): Boolean {
        val requiredPermissions = PermissionsConstans.getPermissionsList().filter {
            ContextCompat.checkSelfPermission(this@SplashActivity, it)  != PackageManager.PERMISSION_GRANTED
        }
        return requiredPermissions.isEmpty()
    }
}