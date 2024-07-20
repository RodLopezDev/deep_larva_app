package com.rodrigo.deeplarva.routes.activity.view

import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.rodrigo.deeplarva.databinding.ActivityCameraBinding
import com.rodrigo.deeplarva.domain.view.CameraValues
import com.rodrigo.deeplarva.utils.Dimensions
import kotlin.math.abs

class CameraActivityView(
    activity: AppCompatActivity,
    cameraValues: CameraValues,
    private val listener: ICameraViewListener
) {
    private var showControl = false
    private val binding = ActivityCameraBinding.inflate(activity.layoutInflater)
    init {
        activity.setContentView(binding.root)
        binding.cameraCaptureButton.setOnClickListener {
            listener.onTakePicture()
        }
        binding.btnShowControl.setOnClickListener {
            val newDim = Dimensions(activity).dpToPx(if(showControl) { 156F } else { 0F })
            binding.llCommandControl.translationX = newDim
            showControl = !showControl
            binding.btnShowControl.text = "Controles (${if(showControl) "-" else "+"})"
        }
        binding.btnClose.setOnClickListener {
            listener.onClose()
        }
        binding.btnDownExposure.setOnClickListener {
            val minValue = cameraValues.exposureMin
            var value = cameraValues.exposure
            if(value <= minValue) {
                return@setOnClickListener
            }
            val factor = abs(minValue / 5)
            value -= factor
            listener.onUpdateExposure(value)
            updateView(cameraValues)
        }
        binding.btnUpExposure.setOnClickListener {
            val maxValue = cameraValues.exposureMax
            var value = cameraValues.exposure
            if(value >= maxValue) {
                return@setOnClickListener
            }
            val factor = maxValue / 5
            value += factor
            listener.onUpdateExposure(value)
            updateView(cameraValues)
        }
        binding.btnDownISO.setOnClickListener {
            val diff = cameraValues.sensorSensitivityMax - cameraValues.sensorSensitivityMin
            var value = cameraValues.sensorSensitivity
            if(value <= cameraValues.sensorSensitivityMin) {
                return@setOnClickListener
            }
            val factor = abs(diff / 5)
            value -= factor
            if(value <= cameraValues.sensorSensitivityMin) {
                value = cameraValues.sensorSensitivityMin
            }
            listener.onUpdateSensitivitySensor(value)
            updateView(cameraValues)
        }
        binding.btnUpISO.setOnClickListener {
            val diff = cameraValues.sensorSensitivityMax - cameraValues.sensorSensitivityMin
            var value = cameraValues.sensorSensitivity
            if(value >= cameraValues.sensorSensitivityMax) {
                return@setOnClickListener
            }
            val factor = diff / 5
            value += factor
            if(value >= cameraValues.sensorSensitivityMax) {
                value = cameraValues.sensorSensitivityMax
            }
            listener.onUpdateSensitivitySensor(value)
            updateView(cameraValues)
        }
        binding.btnDownSpeed.setOnClickListener {
            val diff = cameraValues.shootSpeedMax - cameraValues.shootSpeedMin
            var value = cameraValues.shootSpeed
            if(value <= cameraValues.shootSpeedMin) {
                return@setOnClickListener
            }
            val factor = abs(diff / 5)
            value -= factor
            if(value <= cameraValues.shootSpeedMin) {
                value = cameraValues.shootSpeedMin
            }
            listener.onUpdateShootSpeed(value)
            updateView(cameraValues)
        }
        binding.btnUpSpeed.setOnClickListener {
            val diff = cameraValues.shootSpeedMax - cameraValues.shootSpeedMin
            var value = cameraValues.shootSpeed
            if(value >= cameraValues.shootSpeedMax) {
                return@setOnClickListener
            }
            val factor = abs( diff / 5)
            value += factor
            if(value >= cameraValues.shootSpeedMax) {
                value = cameraValues.shootSpeedMax
            }
            listener.onUpdateShootSpeed(value)
            updateView(cameraValues)
        }
        updateView(cameraValues)
    }
    private fun updateView(cameraValues: CameraValues) {
        binding.tvExposure.text = cameraValues.exposure.toString()
        binding.tvSpeed.text = cameraValues.shootSpeed.toString()
        binding.tvISO.text = cameraValues.sensorSensitivity.toString()
    }
    fun getPreview(): TextureView {
        return binding.textureView
    }
    fun getLinearLayout(): ConstraintLayout {
        return binding.clMain
    }
}