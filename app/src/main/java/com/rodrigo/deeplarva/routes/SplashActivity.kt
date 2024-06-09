package com.rodrigo.deeplarva.routes

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.domain.Constants
import com.rodrigo.deeplarva.utils.PreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class SplashActivity: AppCompatActivity() {
    private val splashScreenTime: Long = 2000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spash_screen)

        val helper = PreferencesHelper(this)
        val identifierExists = helper.getString(Constants.SHARED_PREFERENCES_DEVICE_ID)
        if(identifierExists == null) {
            helper.saveString(Constants.SHARED_PREFERENCES_DEVICE_ID, UUID.randomUUID().toString())
        }

        GlobalScope.launch {
            delay(splashScreenTime)
            withContext(Dispatchers.Main) {
                var intent = Intent(this@SplashActivity,
                    if (grantedPermissions()) PicturesActivity::class.java else PermissionsHandlerActivity::class.java
                )
                startActivity(intent)
                finish()
            }
        }
    }

    private fun grantedPermissions (): Boolean {
        val requiredPermissions = Constants.getPermissionsList().filter {
            ContextCompat.checkSelfPermission(this@SplashActivity, it)  != PackageManager.PERMISSION_GRANTED
        }
        return requiredPermissions.isEmpty()
    }
}