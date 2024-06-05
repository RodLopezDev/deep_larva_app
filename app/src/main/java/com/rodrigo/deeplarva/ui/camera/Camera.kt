package com.rodrigo.deeplarva.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.util.Range
import android.util.Size
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class Camera(private var activity: AppCompatActivity) {

    private var manager: CameraManager
    private var cameraId: String

    var largest: Size private set
    var exposureRange: Range<Int>? private set
    var exposureStep: Float private set
    var isoRange: Range<Int>? private set
    var speedRange: Range<Long>? private set

    init {
        manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = manager.cameraIdList[0]

        val characteristics = manager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        largest = map!!.getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.width * it.height }!!

        exposureRange = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
        exposureStep = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)?.toFloat() ?: 0f
        isoRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        speedRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
    }

    @RequiresPermission(android.Manifest.permission.CAMERA)
    fun openCamera(stateCallback: CameraDevice.StateCallback, backgroundHandler: Handler?) {
        manager.openCamera(cameraId, stateCallback, backgroundHandler)
    }
}