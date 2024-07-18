package com.rodrigo.deeplarva.routes.activity.view

import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import com.rodrigo.deeplarva.databinding.ActivityCameraBinding
import com.rodrigo.deeplarva.utils.Dimensions
import kotlin.math.abs

class CameraActivityView(
    activity: AppCompatActivity,
    private val listener: ICameraViewListener
) {
    private var showControl = false
    private var exposure = listener.getDefaultExposure()
    private val binding = ActivityCameraBinding.inflate(activity.layoutInflater)
    init {
        activity.setContentView(binding.root)
        binding.cameraCaptureButton.setOnClickListener {
            listener.onTakePicture()
        }
        binding.btnShowControl.setOnClickListener {
            val newDim = Dimensions(activity).dpToPx(if(showControl) { 52F } else { 0F })
            binding.llCommandControl.translationX = newDim
            showControl = !showControl
            binding.btnShowControl.text = "Controles (${if(showControl) "-" else "+"})"
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
        return binding.viewPreview
    }
    fun getLinearLayout(): ConstraintLayout {
        return binding.clMain
    }
}