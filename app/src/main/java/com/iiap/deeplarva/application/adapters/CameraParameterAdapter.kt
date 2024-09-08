package com.iiap.deeplarva.application.adapters

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.util.Size
import com.iiap.deeplarva.domain.constants.MessagesConstants
import com.iiap.deeplarva.domain.constants.SharedPreferencesConstants
import com.iiap.deeplarva.domain.view.CameraValues
import com.iiap.deeplarva.utils.PreferencesHelper
import java.math.BigDecimal
import java.math.RoundingMode

class CameraParameterAdapter(
    private val preferencesHelper: PreferencesHelper,
    private val cameraCharacteristics: CameraCharacteristics
) {
    companion object {
        const val MAX_ISO = 3200
        const val MIN_ISO = 100

        const val MIN_SHOOT_SPEED = 100000L
    }

    private lateinit var cameraValues: CameraValues

    init {
        if(
            !preferencesHelper.exists(SharedPreferencesConstants.RESOLUTION_MAX_WIDTH) or
            !preferencesHelper.exists(SharedPreferencesConstants.RESOLUTION_MAX_HEIGHT) or
            !preferencesHelper.exists(SharedPreferencesConstants.EXPOSURE_VALUE) or
            !preferencesHelper.exists(SharedPreferencesConstants.EXPOSURE_MIN) or
            !preferencesHelper.exists(SharedPreferencesConstants.EXPOSURE_MAX) or
            !preferencesHelper.exists(SharedPreferencesConstants.SENSITIVITY_VALUE) or
            !preferencesHelper.exists(SharedPreferencesConstants.SENSITIVITY_MIN) or
            !preferencesHelper.exists(SharedPreferencesConstants.SENSITIVITY_MAX) or
            !preferencesHelper.exists(SharedPreferencesConstants.EXPOSURE_TIME_VALUE) or
            !preferencesHelper.exists(SharedPreferencesConstants.EXPOSURE_TIME_TEXT)
        ) {
            val exposureRange = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
            val exposureTimeRange = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
            val streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val availableDimensions = streamConfigurationMap?.getOutputSizes(ImageFormat.JPEG)
            val dimsWith916 = getDimension916(availableDimensions!!)
            val largest916Size = dimsWith916?.maxByOrNull { it.width * it.height }

            val maxWidth = if(largest916Size!!.width > largest916Size.height) { largest916Size.height } else { largest916Size.width }
            val maxHeight = if(largest916Size!!.width > largest916Size.height) { largest916Size.width } else { largest916Size.height }

            val minExposureTime = exposureTimeRange?.lower ?: MIN_SHOOT_SPEED

            preferencesHelper.saveInt(SharedPreferencesConstants.RESOLUTION_MAX_WIDTH, maxWidth)
            preferencesHelper.saveInt(SharedPreferencesConstants.RESOLUTION_MAX_HEIGHT, maxHeight)
            preferencesHelper.saveInt(SharedPreferencesConstants.EXPOSURE_VALUE, 0)
            preferencesHelper.saveInt(SharedPreferencesConstants.EXPOSURE_MIN, exposureRange!!.lower)
            preferencesHelper.saveInt(SharedPreferencesConstants.EXPOSURE_MAX, exposureRange.upper)
            preferencesHelper.saveInt(SharedPreferencesConstants.SENSITIVITY_VALUE, MIN_ISO)
            preferencesHelper.saveInt(SharedPreferencesConstants.SENSITIVITY_MIN, MIN_ISO)
            preferencesHelper.saveInt(SharedPreferencesConstants.SENSITIVITY_MAX, MAX_ISO)
            preferencesHelper.saveLong(SharedPreferencesConstants.EXPOSURE_TIME_VALUE, minExposureTime)
            preferencesHelper.saveString(SharedPreferencesConstants.EXPOSURE_TIME_TEXT, MessagesConstants.DEFAULT_VALUE_SHUTTER_SPEED)
        }
        initValues()
    }

    private fun getDimension916(dimensions: Array<Size>): List<Size> {
        val result = mutableListOf<Size>()
        val globalFactor = BigDecimal((16F / 9F).toDouble()).setScale(3, RoundingMode.HALF_UP).toFloat()
        for(item in dimensions) {
            val factor = BigDecimal(item.width.toDouble() / item.height.toDouble()).setScale(3, RoundingMode.HALF_UP).toFloat()
            if(globalFactor == factor) {
                result.add(item)
            }
        }
        if(result.isEmpty()){
            return dimensions.toList()
        }
        return result
    }

    private fun initValues() {
        val maxWidth = preferencesHelper.getInt(SharedPreferencesConstants.RESOLUTION_MAX_WIDTH, 0)
        val maxHeight = preferencesHelper.getInt(SharedPreferencesConstants.RESOLUTION_MAX_HEIGHT, 0)
        val exposure = preferencesHelper.getInt(SharedPreferencesConstants.EXPOSURE_VALUE, 0)
        val exposureMin = preferencesHelper.getInt(SharedPreferencesConstants.EXPOSURE_MIN, 0)
        val exposureMax = preferencesHelper.getInt(SharedPreferencesConstants.EXPOSURE_MAX, 0)
        val sensorSensitivity = preferencesHelper.getInt(SharedPreferencesConstants.SENSITIVITY_VALUE, 0)
        val sensorSensitivityMin = preferencesHelper.getInt(SharedPreferencesConstants.SENSITIVITY_MIN, 0)
        val sensorSensitivityMax = preferencesHelper.getInt(SharedPreferencesConstants.SENSITIVITY_MAX, 0)
        val shootSpeed = preferencesHelper.getLong(SharedPreferencesConstants.EXPOSURE_TIME_VALUE, 0)
        val shootSpeedText = preferencesHelper.getString(SharedPreferencesConstants.EXPOSURE_TIME_TEXT, "") ?: ""
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
            shootSpeedText
        )
    }

    fun getCameraValues(): CameraValues {
        return cameraValues!!
    }

    fun updateSensitivitySensor(value: Int) {
        cameraValues.sensorSensitivity = value
        preferencesHelper.saveInt(SharedPreferencesConstants.SENSITIVITY_VALUE, value)
    }

    fun updateExposure(value: Int) {
        cameraValues.exposure = value
        preferencesHelper.saveInt(SharedPreferencesConstants.EXPOSURE_VALUE, value)
    }

    fun updateShootSpeed(value: Long, text: String) {
        cameraValues.shootSpeed = value
        preferencesHelper.saveLong(SharedPreferencesConstants.EXPOSURE_TIME_VALUE, value)
        preferencesHelper.saveString(SharedPreferencesConstants.EXPOSURE_TIME_TEXT, text)
    }
}