package com.rodrigo.deeplarva.routes.view

import android.content.Context
import android.hardware.camera2.CaptureRequest
import android.view.TextureView
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.routes.CameraActivity
import com.rodrigo.deeplarva.ui.camera.CameraParameters

class CameraView(private val activity: CameraActivity, private val parameters: CameraParameters) {

    var textureView: TextureView
        private set
    private var exposureSeekBar: SeekBar
    private var exposureValueText: TextView
    private var isoSeekBar: SeekBar
    private var isoValueText: TextView
    private var speedSeekBar: SeekBar
    private var speedValueText: TextView
    private var captureButton: Button
    private var switchButton: Switch

    init {
        textureView = activity.findViewById(R.id.textureView)
        exposureSeekBar = activity.findViewById(R.id.exposureSeekBar)
        exposureValueText = activity.findViewById(R.id.exposureValueText)
        isoSeekBar = activity.findViewById(R.id.isoSeekBar)
        isoValueText = activity.findViewById(R.id.isoValueText)
        speedSeekBar = activity.findViewById(R.id.speedSeekBar)
        speedValueText = activity.findViewById(R.id.speedValueText)
        captureButton = activity.findViewById(R.id.captureButton)
        switchButton = activity.findViewById(R.id.switchButton)


        val prefs = activity.getSharedPreferences(CameraActivity.PREFS_NAME, Context.MODE_PRIVATE)
        switchButton.isChecked = prefs.getBoolean("SHOW_CONTROLS", false)
        setControlVisibility(switchButton.isChecked)

        switchButton.setOnCheckedChangeListener { _, isChecked ->
            setControlVisibility(isChecked)
        }


        exposureSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateExposure(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        isoSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateISO(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        speedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateSpeed(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        captureButton.setOnClickListener {
            //takeMultiplePhotos(3, 1000)
//            takePhoto()
        }
    }

    private fun setControlVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) SeekBar.VISIBLE else SeekBar.GONE
        exposureSeekBar.visibility = visibility
        exposureValueText.visibility = visibility
        isoSeekBar.visibility = visibility
        isoValueText.visibility = visibility
        speedSeekBar.visibility = visibility
        speedValueText.visibility = visibility
    }

    private fun updateExposure(progress: Int) {
//        cameraDevice?.let {
//            val minExposure = parameters.exposureRange?.lower ?: 0
//            val maxExposure = parameters.exposureRange?.upper ?: 0
//            val exposureCompensation = minExposure + (progress * (maxExposure - minExposure) / 100)
//            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, exposureCompensation)
//            val exposureValue = exposureCompensation * cameraParameters.exposureStep
////            exposureValueText.text = "EV: %.1f".format(exposureValue)
//            updatePreview()
//        }
    }


    private fun updateISO(progress: Int) {
//        cameraDevice?.let {
//            val minISO = cameraParameters.isoRange?.lower ?: 0
//            val maxISO = cameraParameters.isoRange?.upper ?: 0
//            val isoValue = minISO + (progress * (maxISO - minISO) / 100)
//            captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, isoValue)
////            isoValueText.text = "ISO: $isoValue"
//            updatePreview()
//        }
    }

    private fun updateSpeed(progress: Int) {
//        cameraDevice?.let {
//            val minSpeed = cameraParameters.speedRange?.lower ?: 0L
//            val maxSpeed = cameraParameters.speedRange?.upper ?: 0L
//            val speedValue = minSpeed + (progress * (maxSpeed - minSpeed) / 100)
//            captureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, speedValue)
////            speedValueText.text = "Speed: 1/${1000000000 / (if (speedValue <= 0) 1 else speedValue)} sec"
//            updatePreview()
//        }
    }

}