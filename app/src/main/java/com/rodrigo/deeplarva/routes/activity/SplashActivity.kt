package com.rodrigo.deeplarva.routes.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.application.usecases.UseCaseDefaultConfigDevice
import com.rodrigo.deeplarva.application.usecases.UseCaseRegisterDeviceId
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.domain.response.AppConfigurationResponse
import com.rodrigo.deeplarva.helpers.PreferencesHelper
import com.rodrigo.deeplarva.infraestructure.services.AppConfigurationServices
import com.rodrigo.deeplarva.modules.requests.RequestListener
import com.rodrigo.deeplarva.utils.DateUtils
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
        val flag = helper.getString(Constants.CLOUD_VALUE_LAST_DATE_CHECKED, "") ?: ""
        if (flag != "" && DateUtils.isSameAsToday(flag)) {
            return false
        }

        AppConfigurationServices().getConfiguration(object: RequestListener<AppConfigurationResponse> {
            override fun onComplete(result: AppConfigurationResponse) {
                helper.saveString(Constants.CLOUD_VALUE_LAST_DATE_CHECKED, DateUtils.getToday())
                helper.saveString(Constants.CLOUD_VALUE_APP_VERSION, result.version)
                helper.saveString(Constants.CLOUD_VALUE_SERVER_URL, result.environment.API_SERVER_URL)
                helper.saveString(Constants.CLOUD_VALUE_SERVER_API_KEY, result.environment.API_SERVER_KEY)
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
        val requiredPermissions = Constants.getPermissionsList().filter {
            ContextCompat.checkSelfPermission(this@SplashActivity, it)  != PackageManager.PERMISSION_GRANTED
        }
        return requiredPermissions.isEmpty()
    }
}