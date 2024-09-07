package com.iiap.deeplarva.routes.activity.splashscreen

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.iiap.deeplarva.R
import com.iiap.deeplarva.application.usecases.app.UseCaseDefaultConfigDevice
import com.iiap.deeplarva.application.usecases.app.UseCaseGetConfigurationFromCloud
import com.iiap.deeplarva.application.usecases.app.UseCaseRegisterDeviceId
import com.iiap.deeplarva.domain.constants.PermissionsConstans
import com.iiap.deeplarva.routes.activity.PermissionsHandlerActivity
import com.iiap.deeplarva.routes.activity.PicturesActivity
import com.iiap.deeplarva.utils.ThemeUtils
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
        loadAppLogo()

        UseCaseRegisterDeviceId(this).execute()
        UseCaseDefaultConfigDevice(this).execute()

        val requiredCloud = UseCaseGetConfigurationFromCloud(this).execute(::launchActivity)
        if(requiredCloud) {
            return
        }

        delayToNextActivity()
    }

    private fun delayToNextActivity() {
        GlobalScope.launch {
            delay(splashScreenTime)
            withContext(Dispatchers.Main) {
                launchActivity()
            }
        }
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
            logo.setImageResource(R.drawable.img_splash_screen)
        } else {
            logo.setImageResource(R.drawable.img_splash_screen)
        }
    }
}