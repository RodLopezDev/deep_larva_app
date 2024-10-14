package com.deeplarva.iiap.gob.pe.routes.activity.configuration

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.deeplarva.iiap.gob.pe.R
import com.deeplarva.iiap.gob.pe.databinding.ActivityConfigurationBinding
import com.deeplarva.iiap.gob.pe.domain.constants.CloudKeysConstants
import com.deeplarva.iiap.gob.pe.domain.constants.ConfigConstants
import com.deeplarva.iiap.gob.pe.utils.PreferencesHelper
import com.deeplarva.iiap.gob.pe.utils.ThemeUtils

class ConfigurationsActivity: AppCompatActivity() {
    private lateinit var binding: ActivityConfigurationBinding
    private lateinit var preferences: PreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.title_configuration)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        preferences = PreferencesHelper(this)
        if(ThemeUtils.isDarkTheme(this)) {
            supportActionBar?.setHomeAsUpIndicator(ThemeUtils.getBackIconDrawable(this))
        }


        val showManualShutterSpeed = preferences.getBoolean(ConfigConstants.CONFIG_SHOW_SHUTTER_SPEED_CUSTOM, false)
        binding.cbManualShutterSpeed.isChecked = showManualShutterSpeed
        binding.cbManualShutterSpeed.setOnCheckedChangeListener { buttonView, isChecked ->
            preferences.saveBoolean(ConfigConstants.CONFIG_SHOW_SHUTTER_SPEED_CUSTOM, isChecked)
        }

        val showManualISO = preferences.getBoolean(ConfigConstants.CONFIG_SHOW_ISO_CUSTOM, false)
        binding.cbManualISO.isChecked = showManualISO
        binding.cbManualISO.setOnCheckedChangeListener { buttonView, isChecked ->
            preferences.saveBoolean(ConfigConstants.CONFIG_SHOW_ISO_CUSTOM, isChecked)
        }

        val hasCloudCameraValues = preferences.getBoolean(CloudKeysConstants.FLAG_CAMERA_CONFIG_EXIST, false)
        if(hasCloudCameraValues) {
            val isoValue = preferences.getInt(CloudKeysConstants.ISO_VALUE, 0)
            val exposureValue = preferences.getInt(CloudKeysConstants.EXPOSURE_VALUE, 0)
            val shutterSpeedValue = preferences.getInt(CloudKeysConstants.SHUTTER_SPEED_VALUE, 0)

            binding.tvCloudCameraValues.text = "\t\t" + getString(R.string.msg_config_available) + "\n\n" +
                    "ISO: ${isoValue}\n" +
                    "Exposure: ${exposureValue}\n" +
                    "Speed: ${shutterSpeedValue}\n"
        } else {
            binding.tvCloudCameraValues.text = getString(R.string.msg_config_not_available)
        }

        val showPreferences = preferences.getBoolean(ConfigConstants.CONFIG_SHOW_PREFERENCES, false)
        binding.cbShowPreferences.isChecked = showManualShutterSpeed
        binding.cbShowPreferences.setOnCheckedChangeListener { buttonView, isChecked ->
            preferences.saveBoolean(ConfigConstants.CONFIG_SHOW_PREFERENCES, isChecked)
            renderPreferencesTable(isChecked)
        }

        renderPreferencesTable(showPreferences)
    }

    private fun renderPreferencesTable(show: Boolean = false) {
        if(!show) {
            binding.tvTitlePreferences.visibility = View.INVISIBLE
            binding.svPreferences.visibility = View.INVISIBLE
            return
        }

        binding.tvTitlePreferences.visibility = View.VISIBLE
        binding.svPreferences.visibility = View.VISIBLE

        val sharedPreferences: SharedPreferences = getSharedPreferences("DeepLarva-Preferences", Context.MODE_PRIVATE)
        val allKeys: Set<String> = sharedPreferences.all.keys
        val map = mutableMapOf<String, Any?>()

        for (key in allKeys) {
            if(key == CloudKeysConstants.SERVER_API_KEY || key == CloudKeysConstants.SERVER_URL){
                map[key] = "PRIVATE"
                continue
            }
            val value = when (sharedPreferences.all[key]) {
                is String -> sharedPreferences.getString(key, null)
                is Int -> sharedPreferences.getInt(key, 0).toString()
                is Boolean -> sharedPreferences.getBoolean(key, false).toString()
                is Float -> sharedPreferences.getFloat(key, 0f).toString()
                is Long -> sharedPreferences.getLong(key, 0L).toString()
                else -> "".toString()
            }
            map[key] = value
        }

        val isDarkMode = ThemeUtils.isDarkTheme(this)
        val hintColor = if(isDarkMode) "#424242" else "#F0F0F0"

        var rowIndex = 0
        for ((key, value) in map) {
            val tableRow = TableRow(this)

            val keyTextView = TextView(this).apply {
                text = key
                setPadding(16, 16, 16, 16)
                setBackgroundResource(R.drawable.table_cell_border)
                setTextColor(ContextCompat.getColor(context, R.color.simpleText))
            }

            val valueTextView = TextView(this).apply {
                text = value.toString()
                setPadding(16, 16, 16, 16)
                setBackgroundResource(R.drawable.table_cell_border)
                setTextColor(ContextCompat.getColor(context, R.color.simpleText))
            }

            tableRow.addView(keyTextView)
            tableRow.addView(valueTextView)

            if (rowIndex % 2 == 1) {
                tableRow.setBackgroundColor(Color.parseColor(hintColor))  // Light gray for odd rows
            }

            binding.tbPreferences.addView(tableRow)

            rowIndex++
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