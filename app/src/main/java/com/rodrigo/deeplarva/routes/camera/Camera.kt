package com.rodrigo.deeplarva.routes.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.util.Range
import android.util.Size
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.routes.camera.interfaces.CameraRenderListener

class Camera(private var activity: AppCompatActivity, private val listener: CameraRenderListener) {

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
    fun openCamera(backgroundHandler: Handler?) {
        manager.openCamera(cameraId, stateCallback, backgroundHandler)
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            listener.onOpened(this@Camera, camera)
        }
        override fun onDisconnected(camera: CameraDevice) {
            listener.onDisconnected(camera)
        }
        override fun onError(camera: CameraDevice, error: Int) {
            listener.onError(camera, error)
        }
    }

    fun getAdjustedExposure(ev: Int): Int {
        val minExposure = exposureRange?.lower ?: 0
        val maxExposure = exposureRange?.upper ?: 0
        val exposureCompensation = minExposure + (ev * (maxExposure - minExposure) / 100)
        return exposureCompensation.toInt()
    }



    fun getAdjustedISO(iso: Int): Int {
        val minISO = isoRange?.lower ?: 0
        val maxISO = isoRange?.upper ?: 0
        val isoValue = minISO + (iso * (maxISO - minISO) / 100)
        return isoValue
    }

    fun getAdjustedSpeed(speed: Long): Long {
        val minSpeed = speedRange?.lower ?: 0L
        val maxSpeed = speedRange?.upper ?: 0L
        val speedValue = minSpeed + (speed * (maxSpeed - minSpeed) / 100)
        return speedValue
    }
}