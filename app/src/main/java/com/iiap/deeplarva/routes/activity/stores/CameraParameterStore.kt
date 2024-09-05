package com.rodrigo.deeplarva.routes.activity.stores

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.domain.view.CameraValues
import com.rodrigo.deeplarva.helpers.PreferencesHelper
import java.math.BigDecimal
import java.math.RoundingMode

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
            val exposureTimeRange = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
            val streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val availableDimensions = streamConfigurationMap?.getOutputSizes(ImageFormat.JPEG)
            val dimsWith916 = getDimension916(availableDimensions!!)
            val largest916Size = dimsWith916?.maxByOrNull { it.width * it.height }

            val maxWidth = if(largest916Size!!.width > largest916Size.height) { largest916Size.height } else { largest916Size.width }
            val maxHeight = if(largest916Size!!.width > largest916Size.height) { largest916Size.width } else { largest916Size.height }

            val maxExposureTime = exposureTimeRange?.upper ?: Constants.MIN_SHOOT_SPEED
            val minExposureTime = exposureTimeRange?.lower ?: Constants.MIN_SHOOT_SPEED

            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_RESOLUTION_MAX_WIDTH, maxWidth)
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_RESOLUTION_MAX_HEIGHT, maxHeight)
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_EXPOSURE_VALUE, 0)
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_EXPOSURE_MIN, exposureRange!!.lower)
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_EXPOSURE_MAX, exposureRange.upper)
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_VALUE, Constants.MIN_ISO)
            preferencesHelper.saveLong(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_VALUE, minExposureTime)
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_MIN, Constants.MIN_ISO)
            preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_MAX, Constants.MAX_ISO)
            preferencesHelper.saveLong(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_MIN, minExposureTime)
            preferencesHelper.saveLong(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_MAX, maxExposureTime)
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
        val maxWidth = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_RESOLUTION_MAX_WIDTH, 0)
        val maxHeight = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_RESOLUTION_MAX_HEIGHT, 0)
        val exposure = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_EXPOSURE_VALUE, 0)
        val exposureMin = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_EXPOSURE_MIN, 0)
        val exposureMax = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_EXPOSURE_MAX, 0)
        val sensorSensitivity = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_VALUE, 0)
        val sensorSensitivityMin = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_MIN, 0)
        val sensorSensitivityMax = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_SENSOR_SENSITIVITY_MAX, 0)
        val shootSpeed = preferencesHelper.getLong(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_VALUE, 0)
        val shootSpeedMin = preferencesHelper.getLong(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_MIN, 0)
        val shootSpeedMax = preferencesHelper.getLong(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_MAX, 0)
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

    fun updateShootSpeed(value: Long) {
        cameraValues.shootSpeed = value
        preferencesHelper.saveLong(Constants.SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_VALUE, value)
    }
}