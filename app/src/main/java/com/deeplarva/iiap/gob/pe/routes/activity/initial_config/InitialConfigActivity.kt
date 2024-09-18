package com.deeplarva.iiap.gob.pe.routes.activity.splash_screen

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.deeplarva.iiap.gob.pe.R
import com.deeplarva.iiap.gob.pe.application.usecases.app.UseCaseDefaultConfigDevice
import com.deeplarva.iiap.gob.pe.application.usecases.cloud.UseCaseGetCameraConfiguration
import com.deeplarva.iiap.gob.pe.application.usecases.cloud.UseCaseGetConfigurationFromCloud
import com.deeplarva.iiap.gob.pe.application.usecases.app.UseCaseRegisterDeviceId
import com.deeplarva.iiap.gob.pe.domain.constants.PermissionsConstans
import com.deeplarva.iiap.gob.pe.infraestructure.services.AppConfigurationServices
import com.deeplarva.iiap.gob.pe.routes.activity.main.PicturesActivity
import com.deeplarva.iiap.gob.pe.routes.activity.permissions.PermissionsHandlerActivity
import com.deeplarva.iiap.gob.pe.utils.PreferencesHelper
import com.deeplarva.iiap.gob.pe.utils.ThemeUtils
import com.deeplarva.iiap.gob.pe.utils.VersionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity: AppCompatActivity() {
    private val splashScreenTime: Long = 2000
    private val appConfigServices = AppConfigurationServices()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spash_screen)
        loadAppLogo()


        val preferences = PreferencesHelper(this)

        UseCaseRegisterDeviceId(preferences).execute()
        UseCaseDefaultConfigDevice(preferences).execute()

        val version = VersionUtils.getAppVersion(this)
        val requiredCloud = UseCaseGetConfigurationFromCloud(version, preferences, appConfigServices)
            .execute(::getCameraConfig)
        if(requiredCloud) {
            return
        }

        getCameraConfig()
    }

    private fun delayToNextActivity() {
        GlobalScope.launch {
            delay(splashScreenTime)
            withContext(Dispatchers.Main) {
                launchActivity()
            }
        }
    }

    private fun getCameraConfig() {
        val brand = android.os.Build.BRAND
        val model = android.os.Build.MODEL
        val preferences = PreferencesHelper(this)
        val requiredCloud = UseCaseGetCameraConfiguration(brand, model, preferences, appConfigServices)
            .execute(::launchActivity)
        if(requiredCloud) {
            return
        }

        delayToNextActivity()
    }

    private fun launchActivity() {
        val nextActivity = if (grantedPermissions()) PicturesActivity::class.java else PermissionsHandlerActivity::class.java
        var intent = Intent(this@SplashActivity, nextActivity)
        startActivity(intent)
        finish()
    }

    private fun grantedPermissions (): Boolean {
        val requiredPermissions = PermissionsConstans.getPermissionsList().filter {
            ContextCompat.checkSelfPermission(this@SplashActivity, it)  != PackageManager.PERMISSION_GRANTED
        }
        return requiredPermissions.isEmpty()
    }

    private fun loadAppLogo() {
        val logo = findViewById<ImageView>(R.id.centered_image)
        if(ThemeUtils.isDarkTheme(this)) {
            logo.setImageResource(R.drawable.img_splash_screen_dark)
        } else {
            logo.setImageResource(R.drawable.img_splash_screen)
        }
    }
}