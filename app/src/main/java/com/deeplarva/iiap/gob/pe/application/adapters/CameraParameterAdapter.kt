package com.deeplarva.iiap.gob.pe.application.adapters

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.util.Size
import com.deeplarva.iiap.gob.pe.domain.constants.MessagesConstants
import com.deeplarva.iiap.gob.pe.domain.constants.SharedPreferencesConstants
import com.deeplarva.iiap.gob.pe.domain.view.CameraValues
import com.deeplarva.iiap.gob.pe.utils.ExposureUtils
import com.deeplarva.iiap.gob.pe.utils.PreferencesHelper
import java.math.BigDecimal
import java.math.RoundingMode

class CameraParameterAdapter(
    private val preferencesHelper: PreferencesHelper,
    private val cameraCharacteristics: CameraCharacteristics
) {
    companion object {
        const val DEFAULT_EXPOSURE = 0
        const val EXPOSURE_FACTOR = 4

        const val EXPOSURE_MIN = -5//-20
        const val EXPOSURE_MAX = 5//20
        const val DEFAULT_ISO = 0
        const val DEFAULT_SHUTTER_SPEED = 0
    }

    private lateinit var cameraValues: CameraValues

    init {
        if(
            !preferencesHelper.exists(SharedPreferencesConstants.RESOLUTION_MAX_WIDTH) or
            !preferencesHelper.exists(SharedPreferencesConstants.RESOLUTION_MAX_HEIGHT) or
            !preferencesHelper.exists(SharedPreferencesConstants.EXPOSURE_STEP) or
            !preferencesHelper.exists(SharedPreferencesConstants.EXPOSURE_VALID_STEP)
        ) {
            val streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val exposureCompensationStep = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)

            val availableDimensions = streamConfigurationMap?.getOutputSizes(ImageFormat.JPEG)
            val dimsWith916 = getDimension916(availableDimensions!!)
            val largest916Size = dimsWith916?.maxByOrNull { it.width * it.height }

            val maxWidth = if(largest916Size!!.width > largest916Size.height) { largest916Size.height } else { largest916Size.width }
            val maxHeight = if(largest916Size!!.width > largest916Size.height) { largest916Size.width } else { largest916Size.height }

            val exposureStep = exposureCompensationStep?.toFloat() ?: 0F
            val exposureValidStep = ExposureUtils.expoStepToValidStep(exposureStep)

            preferencesHelper.saveFloat(SharedPreferencesConstants.EXPOSURE_STEP, exposureStep)
            preferencesHelper.saveFloat(SharedPreferencesConstants.EXPOSURE_VALID_STEP, exposureValidStep)
            preferencesHelper.saveInt(SharedPreferencesConstants.RESOLUTION_MAX_WIDTH, maxWidth)
            preferencesHelper.saveInt(SharedPreferencesConstants.RESOLUTION_MAX_HEIGHT, maxHeight)
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

        val exposureStep = preferencesHelper.getFloat(SharedPreferencesConstants.EXPOSURE_STEP)
        val exposureValidStep = preferencesHelper.getFloat(SharedPreferencesConstants.EXPOSURE_VALID_STEP)

        cameraValues = CameraValues(
            maxWidth,
            maxHeight,
            sensorSensitivity,
            exposureStep,
            exposureValidStep,
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