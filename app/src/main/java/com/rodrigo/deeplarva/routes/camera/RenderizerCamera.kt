package com.rodrigo.deeplarva.routes.camera

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.MeteringRectangle
import android.media.Image
import android.media.ImageReader
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.routes.camera.interfaces.CameraActionListener
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

class RenderizerCamera(private val activity: AppCompatActivity) {

    private val TAG = "RenderizerCamera"
    private var cameraDevice: CameraDevice? = null

    private var camera: Camera? = null
    private var captureSession: CameraCaptureSession? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null

    fun render(cameraProp: Camera, cameraDeviceProp: CameraDevice, textureView: TextureView, handler: Handler, imageReader: ImageReader) {
        camera = cameraProp
        cameraDevice = cameraDeviceProp
        preview(textureView, handler, imageReader)
    }
    fun delete(camera: CameraDevice) {
        cameraDevice?.close()
        cameraDevice = null
    }

    private fun preview(textureView: TextureView, handler: Handler, imageReader: ImageReader) {
        try {
            val texture = textureView.surfaceTexture!!
            val previewSize = camera!!.largest
//            textureView.post {
//                adjustAspectRatio(textureView, previewSize.width, previewSize.height)
//            }
//
            texture.setDefaultBufferSize(previewSize.width, previewSize.height)

            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder!!.addTarget(surface)
            captureRequestBuilder!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF)


            cameraDevice!!.createCaptureSession(listOf(surface, imageReader.surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    if (cameraDevice == null) return
                    captureSession = cameraCaptureSession
                    initializeUpdate(handler)
                }
                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    Toast.makeText(activity, "Configuration change", Toast.LENGTH_SHORT).show()
                }
            }, handler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating camera preview: ${e.message}")
        }
    }

    private fun initializeUpdate(handler: Handler) {
        if (camera == null) return
        if (cameraDevice == null) return
        if (captureSession == null) return
        if (captureRequestBuilder == null) return

        captureRequestBuilder!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF)
        try {
            captureSession!!.setRepeatingRequest(captureRequestBuilder!!.build(), null, handler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating preview: ${e.message}")
        }
    }

    fun updateRender(savedEV: Int, savedISO: Int, savedSpeed: Long, handler: Handler) {
        if (camera == null) return
        if (cameraDevice == null) return
        if (captureSession == null) return
        if (captureRequestBuilder == null) return

        captureRequestBuilder!!.set(CaptureRequest.SENSOR_SENSITIVITY,  camera!!.getAdjustedISO(savedISO))
        captureRequestBuilder!!.set(CaptureRequest.SENSOR_EXPOSURE_TIME,  camera!!.getAdjustedSpeed(savedSpeed))
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, camera!!.getAdjustedExposure(savedEV))

        captureRequestBuilder!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF)
        try {
            captureSession!!.setRepeatingRequest(captureRequestBuilder!!.build(), null, handler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating preview: ${e.message}")
        }
    }

    fun onTouch(x: Float, y: Float, textureView: TextureView, handler: Handler) {
        if(cameraDevice == null) return
        if(captureRequestBuilder == null) return

        val characteristics = (activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager)
            .getCameraCharacteristics(cameraDevice!!.id)
        val sensorArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?: return

        val focusAreaSize = 200
        val focusArea = Rect(
            (x / textureView.width * sensorArraySize.width()).toInt() - focusAreaSize / 2,
            (y / textureView.height * sensorArraySize.height()).toInt() - focusAreaSize / 2,
            (x / textureView.width * sensorArraySize.width()).toInt() + focusAreaSize / 2,
            (y / textureView.height * sensorArraySize.height()).toInt() + focusAreaSize / 2
        )

        captureRequestBuilder!!.set(CaptureRequest.CONTROL_AF_REGIONS, arrayOf(MeteringRectangle(focusArea, MeteringRectangle.METERING_WEIGHT_MAX)))
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)

        captureSession!!.capture(captureRequestBuilder!!.build(), object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)
                captureRequestBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE)
                initializeUpdate(handler)
            }
        }, handler)
    }

    fun takePicture(listener: CameraActionListener, imageReader: ImageReader,handler: Handler) {
        if(captureRequestBuilder == null) return
        try {
            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(imageReader.surface)
                set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, captureRequestBuilder!!.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION) ?: 0)
                set(CaptureRequest.SENSOR_SENSITIVITY, captureRequestBuilder!!.get(CaptureRequest.SENSOR_SENSITIVITY) ?: 100)
                set(CaptureRequest.SENSOR_EXPOSURE_TIME, captureRequestBuilder!!.get(CaptureRequest.SENSOR_EXPOSURE_TIME) ?: 100000000L)
                set(CaptureRequest.JPEG_ORIENTATION, 90)
            }

            captureSession!!.capture(captureBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    var img: Image? = null
                    imageReader.acquireLatestImage().use { image ->
                        if(image == null) return@use
//                        val buffer: ByteBuffer = image.planes[0].buffer
//                        val bytes = ByteArray(buffer.capacity())
//                        buffer.get(bytes)
                        img = image
                    }
                    if(img == null) {
                        return
                    }
                    listener.onReceivePicture(img!!)
                }
                override fun onCaptureFailed(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    failure: CaptureFailure
                ) {
                    super.onCaptureFailed(session, request, failure)
                    listener.onFailReceivePicture()
                }
            }, handler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "Error taking photo: ${e.message}")
        }
    }
}