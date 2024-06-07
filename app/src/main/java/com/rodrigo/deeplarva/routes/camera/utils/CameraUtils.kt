package com.rodrigo.deeplarva.routes.camera.utils

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.media.Image
import android.widget.Toast
import com.rodrigo.deeplarva.routes.camera.domain.CameraCharacteristic
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class CameraUtils {
    companion object {
        fun getCameraCharacteristic(cameraCharacteristics: CameraCharacteristics): CameraCharacteristic {

            val map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val largest = map!!.getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.width * it.height }!!
            val exposureRange = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
            val exposureStep = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)?.toFloat() ?: 0f
            val isoRange = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
            val speedRange = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)

            // NOTE: DEFAULT VALUES IS 100%
//            val defaultExposureTime = speedRange?.upper ?: 0L
//            val defaultIso = isoRange?.upper ?: 0
//            val defaultExposure = exposureRange?.upper ?: 0

            return CameraCharacteristic(largest, exposureRange!!, exposureStep, isoRange!!, speedRange!!)
        }
    }
}