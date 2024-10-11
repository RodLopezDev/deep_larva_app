package com.deeplarva.iiap.gob.pe.routes.activity.initial_config

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.deeplarva.iiap.gob.pe.R
import com.deeplarva.iiap.gob.pe.application.usecases.app.UseCaseDefaultConfigDevice
import com.deeplarva.iiap.gob.pe.application.usecases.app.UseCaseRegisterDeviceId
import com.deeplarva.iiap.gob.pe.application.usecases.cloud.UseCaseGetCameraConfiguration
import com.deeplarva.iiap.gob.pe.application.usecases.cloud.UseCaseGetConfigurationFromCloud
import com.deeplarva.iiap.gob.pe.databinding.ActivityInitialConfigBinding
import com.deeplarva.iiap.gob.pe.domain.constants.PermissionsConstans
import com.deeplarva.iiap.gob.pe.infraestructure.services.AppConfigurationServices
import com.deeplarva.iiap.gob.pe.routes.activity.main.PicturesActivity
import com.deeplarva.iiap.gob.pe.routes.activity.permissions.PermissionsHandlerActivity
import com.deeplarva.iiap.gob.pe.utils.PreferencesHelper
import com.deeplarva.iiap.gob.pe.utils.ThemeUtils
import com.deeplarva.iiap.gob.pe.utils.VersionUtils

class InitialConfigActivity: AppCompatActivity() {
    private lateinit var binding: ActivityInitialConfigBinding

    private val appConfigServices = AppConfigurationServices()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInitialConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val preferences = PreferencesHelper(this)
        binding.tvInfo.text = getString(R.string.msg_bootstrap_config_app)

        UseCaseRegisterDeviceId(preferences).execute()
        UseCaseDefaultConfigDevice(preferences).execute()

        val version = VersionUtils.getAppVersion(this)
        val requiredCloud = UseCaseGetConfigurationFromCloud(version, preferences, appConfigServices)
            .execute(::getCameraConfig)
        if(requiredCloud) {
            runOnUiThread {
                binding.tvInfo.text = getString(R.string.msg_bootstrap_config_remote)
            }
            return
        }

        getCameraConfig()
    }

    private fun getCameraConfig() {
        val brand = android.os.Build.BRAND
        val model = android.os.Build.MODEL
        val preferences = PreferencesHelper(this)
        val requiredCloud = UseCaseGetCameraConfiguration(brand, model, preferences, appConfigServices)
            .execute(::launchActivity)
        if(requiredCloud) {
            runOnUiThread {
                binding.tvInfo.text = getString(R.string.msg_bootstrap_config_camera)
            }
            return
        }

        launchActivity()
    }

    private fun launchActivity() {
        val nextActivity = if (grantedPermissions()) PicturesActivity::class.java else PermissionsHandlerActivity::class.java
        var intent = Intent(this@InitialConfigActivity, nextActivity)
        startActivity(intent)
        finish()
    }

    private fun grantedPermissions (): Boolean {
        val requiredPermissions = PermissionsConstans.getPermissionsList().filter {
            ContextCompat.checkSelfPermission(this@InitialConfigActivity, it)  != PackageManager.PERMISSION_GRANTED
        }
        return requiredPermissions.isEmpty()
    }

    private fun loadAppLogo() {
        val logo = ImageView(this)
        if(ThemeUtils.isDarkTheme(this)) {
            logo.setImageResource(R.drawable.img_splash_screen_dark)
        } else {
            logo.setImageResource(R.drawable.img_splash_screen)
        }
    }
}