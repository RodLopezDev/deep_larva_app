package com.rodrigo.deeplarva.routes.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.databinding.ActivityConfigurationBinding
import com.rodrigo.deeplarva.helpers.PreferencesHelper

class ConfigurationsActivity: AppCompatActivity() {
    private lateinit var binding: ActivityConfigurationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Configuraciones"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val helper = PreferencesHelper(this)

        val isActiveCameraV2 = helper.getBoolean(Constants.CONFIG_SHARED_PREFERENCES_CAMERA_ACTIVITY_V2, false)

        binding.cbCameraV2.isChecked = isActiveCameraV2

        binding.cbCameraV2.setOnCheckedChangeListener { buttonView, isChecked ->
            helper.saveBoolean(Constants.CONFIG_SHARED_PREFERENCES_CAMERA_ACTIVITY_V2, isChecked)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}