package com.iiap.deeplarva.routes.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.iiap.deeplarva.databinding.ActivityConfigurationBinding
import com.iiap.deeplarva.domain.constants.SharedPreferencesConstants
import com.iiap.deeplarva.utils.PreferencesHelper

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

        val isActiveCameraV2 = helper.getBoolean(SharedPreferencesConstants.CONFIG_CAMERA_ACTIVITY_V2, false)

        binding.cbCameraV2.isChecked = isActiveCameraV2

        binding.cbCameraV2.setOnCheckedChangeListener { buttonView, isChecked ->
            helper.saveBoolean(SharedPreferencesConstants.CONFIG_CAMERA_ACTIVITY_V2, isChecked)
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