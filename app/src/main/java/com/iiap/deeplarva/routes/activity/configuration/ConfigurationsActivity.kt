package com.iiap.deeplarva.routes.activity.configuration

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.iiap.deeplarva.databinding.ActivityConfigurationBinding
import com.iiap.deeplarva.domain.constants.ConfigConstants
import com.iiap.deeplarva.utils.PreferencesHelper
import com.iiap.deeplarva.utils.ThemeUtils

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

        if(ThemeUtils.isDarkTheme(this)) {
            supportActionBar?.setHomeAsUpIndicator(ThemeUtils.getBackIconDrawable(this))
        }

        val helper = PreferencesHelper(this)

        val showManualShutterSpeed = helper.getBoolean(ConfigConstants.CONFIG_SHOW_SHUTTER_SPEED_CUSTOM)
        binding.cbManualShutterSpeed.isChecked = showManualShutterSpeed
        binding.cbManualShutterSpeed.setOnCheckedChangeListener { buttonView, isChecked ->
            helper.saveBoolean(ConfigConstants.CONFIG_SHOW_SHUTTER_SPEED_CUSTOM, isChecked)
        }

        val showManualISO = helper.getBoolean(ConfigConstants.CONFIG_SHOW_ISO_CUSTOM)
        binding.cbManualISO.isChecked = showManualISO
        binding.cbManualISO.setOnCheckedChangeListener { buttonView, isChecked ->
            helper.saveBoolean(ConfigConstants.CONFIG_SHOW_ISO_CUSTOM, isChecked)
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