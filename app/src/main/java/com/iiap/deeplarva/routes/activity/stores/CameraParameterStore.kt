package com.iiap.deeplarva.routes.activity.stores

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.iiap.deeplarva.application.utils.Constants
import com.iiap.deeplarva.domain.constants.SharedPreferencesConstants
import com.iiap.deeplarva.domain.view.CameraValues
import com.iiap.deeplarva.helpers.PreferencesHelper
import java.math.BigDecimal
import java.math.RoundingMode

class CameraParameterStore(private val activity: AppCompatActivity) {
    private lateinit var cameraValues: CameraValues
    private val preferencesHelper = PreferencesHelper(activity)

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
            !preferencesHelper.exists(SharedPreferencesConstants.EXPOSURE_TIME_MIN) or
            !preferencesHelper.exists(SharedPreferencesConstants.EXPOSURE_TIME_MAX)
        ) {
            val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)

            val exposureRange = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
            val exposureTimeRange = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
            val streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val availableDimensions = streamConfigurationMap?.getOutputSizes(ImageFormat.JPEG)
            val dimsWith916 = getDimension916(availableDimensions!!)
            val largest916Size = dimsWith916?.maxByOrNull { it.width * it.height }

            val maxWidth = if(largest916Size!!.width > largest916Size.height) { largest916Size.height } else { largest916Size.width }
            val maxHeight = if(largest916Size!!.width > largest916Size.height) { largest916Size.width } else { largest916Size.height }

            val maxExposureTime = exposureTimeRange?.upper ?: Constants.MIN_SHOOT_SPEED
            val minExposureTime = exposureTimeRange?.lower ?: Constants.MIN_SHOOT_SPEED

            preferencesHelper.saveInt(SharedPreferencesConstants.RESOLUTION_MAX_WIDTH, maxWidth)
            preferencesHelper.saveInt(SharedPreferencesConstants.RESOLUTION_MAX_HEIGHT, maxHeight)
            preferencesHelper.saveInt(SharedPreferencesConstants.EXPOSURE_VALUE, 0)
            preferencesHelper.saveInt(SharedPreferencesConstants.EXPOSURE_MIN, exposureRange!!.lower)
            preferencesHelper.saveInt(SharedPreferencesConstants.EXPOSURE_MAX, exposureRange.upper)
            preferencesHelper.saveInt(SharedPreferencesConstants.SENSITIVITY_VALUE, Constants.MIN_ISO)
            preferencesHelper.saveLong(SharedPreferencesConstants.EXPOSURE_TIME_VALUE, minExposureTime)
            preferencesHelper.saveInt(SharedPreferencesConstants.SENSITIVITY_MIN, Constants.MIN_ISO)
            preferencesHelper.saveInt(SharedPreferencesConstants.SENSITIVITY_MAX, Constants.MAX_ISO)
            preferencesHelper.saveLong(SharedPreferencesConstants.EXPOSURE_TIME_MIN, minExposureTime)
            preferencesHelper.saveLong(SharedPreferencesConstants.EXPOSURE_TIME_MAX, maxExposureTime)
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
        val shootSpeedMin = preferencesHelper.getLong(SharedPreferencesConstants.EXPOSURE_TIME_MIN, 0)
        val shootSpeedMax = preferencesHelper.getLong(SharedPreferencesConstants.EXPOSURE_TIME_MAX, 0)
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
        preferencesHelper.saveInt(SharedPreferencesConstants.SENSITIVITY_VALUE, value)
    }

    fun updateExposure(value: Int) {
        cameraValues.exposure = value
        preferencesHelper.saveInt(SharedPreferencesConstants.EXPOSURE_VALUE, value)
    }

    fun updateShootSpeed(value: Long) {
        cameraValues.shootSpeed = value
        preferencesHelper.saveLong(SharedPreferencesConstants.EXPOSURE_TIME_VALUE, value)
    }
}