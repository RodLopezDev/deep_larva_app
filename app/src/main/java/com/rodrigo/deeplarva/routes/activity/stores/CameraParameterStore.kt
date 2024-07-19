package com.rodrigo.deeplarva.routes.activity.stores

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.domain.view.CameraValues
import com.rodrigo.deeplarva.helpers.PreferencesHelper

class CameraParameterStore(private val activity: AppCompatActivity) {
    private lateinit var cameraValues: CameraValues
    private val preferencesHelper = PreferencesHelper(activity)

    init {
        if(
            !preferencesHelper.exists(Constants.SHARED_PREFERENCES_RESOLUTION_MAX_WIDTH) or
            !preferencesHelper.exists(Constants.SHARED_PREFERENCES_RESOLUTION_MAX_HEIGHT) or
            !preferencesHelper.exists(Constants.SHARED_PREFERENCES_EXPOSURE_VALUE) or
            !preferencesHelper.exists(Constants.SHARED_PREFERENCES_EXPOSURE_MIN) or
            !preferencesHelper.exists(Constants.SHARED_PREFERENCES_EXPOSURE_MAX) or
            !preferencesHelper.exists(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_VALUE) or
            !preferencesHelper.exists(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_MIN) or
            !preferencesHelper.exists(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_MAX) or
            !preferencesHelper.exists(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_VALUE) or
            !preferencesHelper.exists(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_MIN) or
            !preferencesHelper.exists(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_MAX)
        ) {
            val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)

            val exposureRange = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
            val isoRange = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
            val speedRange = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
            val streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val availableDimensions = streamConfigurationMap?.getOutputSizes(ImageFormat.JPEG)
            val largestSize = availableDimensions?.maxByOrNull { it.width * it.height }

            val initialISO = isoRange!!.upper
            val initialSpeed = speedRange!!.upper

            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_RESOLUTION_MAX_WIDTH, largestSize!!.width)
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_RESOLUTION_MAX_HEIGHT, largestSize!!.height)
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_EXPOSURE_VALUE, 0)
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_EXPOSURE_MIN, exposureRange!!.lower)
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_EXPOSURE_MAX, exposureRange.upper)
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_VALUE, initialISO!!)
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_MIN, isoRange.lower)
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_MAX, isoRange.upper)
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_VALUE, initialSpeed!!.toInt())
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_MIN, speedRange.lower.toInt())
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_MAX, speedRange.upper.toInt())
        }
        initValues()
    }

    private fun initValues() {
        val maxWidth = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_RESOLUTION_MAX_WIDTH, 0)
        val maxHeight = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_RESOLUTION_MAX_HEIGHT, 0)
        val exposure = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_EXPOSURE_VALUE, 0)
        val exposureMin = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_EXPOSURE_MIN, 0)
        val exposureMax = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_EXPOSURE_MAX, 0)
        val sensorSensitivity = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_VALUE, 0)
        val sensorSensitivityMin = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_MIN, 0)
        val sensorSensitivityMax = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_MAX, 0)
        val shootSpeed = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_VALUE, 0)
        val shootSpeedMin = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_MIN, 0)
        val shootSpeedMax = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_MAX, 0)
        cameraValues = CameraValues(
            maxWidth,
            maxHeight,
            sensorSensitivity,
            sensorSensitivityMin,
            sensorSensitivityMax,
            exposure,
            exposureMin,
            exposureMax,
            shootSpeed,
            shootSpeedMin,
            shootSpeedMax
        )
    }

    fun getCameraValues(): CameraValues {
        return cameraValues!!
    }

    fun updateSensitivitySensor(value: Int) {
        cameraValues.sensorSensitivity = value
        preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_VALUE, value)
    }

    fun updateExposure(value: Int) {
        cameraValues.exposure = value
        preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_EXPOSURE_VALUE, value)
    }

    fun updateShootSpeed(value: Int) {
        cameraValues.shootSpeed = value
        preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_VALUE, value)
    }
}