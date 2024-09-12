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
        const val DEFAULT_EXPOSURE = 0
        const val EXPOSURE_MIN = -4//-20
        const val EXPOSURE_MAX = 4//20
        const val EXPOSURE_MIN_V2 = -20
        const val EXPOSURE_MAX_V2 = 20
        const val DEFAULT_ISO = 0
        const val DEFAULT_SHUTTER_SPEED = 0
    }

    private lateinit var cameraValues: CameraValues

    init {
        if(
            !preferencesHelper.exists(SharedPreferencesConstants.RESOLUTION_MAX_WIDTH) or
            !preferencesHelper.exists(SharedPreferencesConstants.RESOLUTION_MAX_HEIGHT) or
            !preferencesHelper.exists(SharedPreferencesConstants.EXPOSURE_RANGE_MIN) or
            !preferencesHelper.exists(SharedPreferencesConstants.EXPOSURE_RANGE_MAX)
        ) {
            val streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val exposureCompensationRange = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)

            val availableDimensions = streamConfigurationMap?.getOutputSizes(ImageFormat.JPEG)
            val dimsWith916 = getDimension916(availableDimensions!!)
            val largest916Size = dimsWith916?.maxByOrNull { it.width * it.height }

            val maxWidth = if(largest916Size!!.width > largest916Size.height) { largest916Size.height } else { largest916Size.width }
            val maxHeight = if(largest916Size!!.width > largest916Size.height) { largest916Size.width } else { largest916Size.height }

            val exposureMin = if (exposureCompensationRange != null) exposureCompensationRange.lower else EXPOSURE_MIN_V2
            val exposureMax = if (exposureCompensationRange != null) exposureCompensationRange.upper else EXPOSURE_MAX_V2

            preferencesHelper.saveInt(SharedPreferencesConstants.RESOLUTION_MAX_WIDTH, maxWidth)
            preferencesHelper.saveInt(SharedPreferencesConstants.RESOLUTION_MAX_HEIGHT, maxHeight)
            preferencesHelper.saveInt(SharedPreferencesConstants.EXPOSURE_RANGE_MIN, exposureMin)
            preferencesHelper.saveInt(SharedPreferencesConstants.EXPOSURE_RANGE_MAX, exposureMax)
        }
        if(!preferencesHelper.exists(SharedPreferencesConstants.SENSITIVITY_VALUE)) {
            preferencesHelper.saveInt(SharedPreferencesConstants.SENSITIVITY_VALUE, DEFAULT_ISO)
        }
        if(!preferencesHelper.exists(SharedPreferencesConstants.EXPOSURE_VALUE)) {
            preferencesHelper.saveInt(SharedPreferencesConstants.EXPOSURE_VALUE, DEFAULT_EXPOSURE)
        }
        if(!preferencesHelper.exists(SharedPreferencesConstants.SHUTTER_SPEED_TIME_VALUE)) {
            preferencesHelper.saveInt(SharedPreferencesConstants.SHUTTER_SPEED_TIME_VALUE, DEFAULT_SHUTTER_SPEED)
            preferencesHelper.saveString(SharedPreferencesConstants.SHUTTER_SPEED_TIME_TEXT, MessagesConstants.DEFAULT_VALUE_SHUTTER_SPEED)
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
        val sensorSensitivity = preferencesHelper.getInt(SharedPreferencesConstants.SENSITIVITY_VALUE, 0)
        val shootSpeed = preferencesHelper.getInt(SharedPreferencesConstants.SHUTTER_SPEED_TIME_VALUE, 0)
        val shootSpeedText = preferencesHelper.getString(SharedPreferencesConstants.SHUTTER_SPEED_TIME_TEXT, MessagesConstants.DEFAULT_VALUE_SHUTTER_SPEED) ?:MessagesConstants.DEFAULT_VALUE_SHUTTER_SPEED

        val exposureMin = preferencesHelper.getInt(SharedPreferencesConstants.EXPOSURE_RANGE_MIN, 0)
        val exposureMax = preferencesHelper.getInt(SharedPreferencesConstants.EXPOSURE_RANGE_MAX, 0)

        cameraValues = CameraValues(
            maxWidth,
            maxHeight,
            sensorSensitivity,
            exposureMin,
            exposureMax,
            exposure,
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

    fun updateShootSpeed(value: Int, text: String) {
        cameraValues.shootSpeed = value
        preferencesHelper.saveInt(SharedPreferencesConstants.SHUTTER_SPEED_TIME_VALUE, value)
        preferencesHelper.saveString(SharedPreferencesConstants.SHUTTER_SPEED_TIME_TEXT, text)
    }
}