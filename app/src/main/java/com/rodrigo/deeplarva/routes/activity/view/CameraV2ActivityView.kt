package com.rodrigo.deeplarva.routes.activity.view

import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import com.rodrigo.deeplarva.databinding.ActivityCameraV2Binding
import com.rodrigo.deeplarva.utils.Dimensions
import kotlin.math.abs

class CameraV2ActivityView(
    activity: AppCompatActivity,
    private val listener: ICameraV2ViewListener
) {
    private var showControl = false
    private var exposure = listener.getDefaultExposure()
    private val binding = ActivityCameraV2Binding.inflate(activity.layoutInflater)
    init {
        activity.setContentView(binding.root)
        binding.cameraCaptureButton.setOnClickListener {
            listener.onTakePicture()
        }
        binding.btnShowControl.setOnClickListener {
            val newDim = Dimensions(activity).dpToPx(if(showControl) { 52F } else { 0F })
            binding.llCommandControl.translationX = newDim
            showControl = !showControl
        }
        binding.btnClose.setOnClickListener {
            listener.onClose()
        }
        binding.btnDownExposure.setOnClickListener {
            val minValue = listener.getMinExposure()
            if(exposure <= minValue) {
                return@setOnClickListener
            }
            val factor = abs(minValue / 5)
            exposure -= factor
            updateView(exposure)
            listener.onUpdateExposure(exposure)
        }
        binding.btnUpExposure.setOnClickListener {
            val maxValue = listener.getMaxExposure()
            if(exposure >= maxValue) {
                return@setOnClickListener
            }
            val factor = maxValue / 5
            exposure += factor
            updateView(exposure)
            listener.onUpdateExposure(exposure)
        }
        updateView(exposure)
    }
    private fun updateView(expo: Int) {
        binding.tvExposure.text = expo.toString()
    }
    fun getPreview(): PreviewView {
        return binding.viewFinder
    }
    fun getLinearLayout(): ConstraintLayout {
        return binding.clMain
    }
}