package com.deeplarva.iiap.gob.pe.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("DeepLarva-MyPreferences", Context.MODE_PRIVATE)

    fun saveString(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun saveInt(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun saveFloat(key: String, value: Float) {
        val editor = sharedPreferences.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    fun saveBoolean(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun saveLong(key: String, value: Long) {
        val editor = sharedPreferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float = 0F): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    fun exists(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}
