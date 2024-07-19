package com.rodrigo.deeplarva.modules.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.media.ImageReader
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.rodrigo.deeplarva.domain.view.CameraValues

class CameraProHardware(
    private val activity: AppCompatActivity,
    private val textureView: TextureView,
    private val cameraValues: CameraValues,
    private val listener: CameraProHardwareListener
) {
    private lateinit var imageReader: ImageReader
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var cameraCaptureSession: CameraCaptureSession

    fun onStart() {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                openCamera()
            }
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
        }
    }

    private fun openCamera() {
        val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            cameraManager.openCamera(cameraId, stateCallback, null)
        } catch (e: CameraAccessException) {
        }
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreviewSession()
        }
        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
        }
        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
        }
    }
    private fun createCameraPreviewSession() {
        val texture = textureView.surfaceTexture!!
        texture.setDefaultBufferSize(textureView.width, textureView.height)
        val surface = Surface(texture)

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)

        imageReader = ImageReader.newInstance(cameraValues.maxWidth, cameraValues.maxHeight, ImageFormat.JPEG, 1)
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if(image == null) {
                listener.onError("CameraPro.createCameraPreviewSession.imageReader.setOnImageAvailableListener::image is null")
                return@setOnImageAvailableListener
            }
            listener.onReceivePicture(image)
            image.close()
        }, null)

        cameraDevice.createCaptureSession(listOf(surface, imageReader.surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                if (cameraDevice == null) {
                    listener.onError("CameraPro.cameraDevice.createCaptureSession.onConfigured::cameraDevice is null")
                    return
                }
                cameraCaptureSession = session
                updateCameraThread()
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {}
        }, null)
    }

    private fun updateCameraThread() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null)
    }

    fun updateExposure(value: Int){
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, value)
        updateCameraThread()
    }

    fun updateISO(value: Int){
        captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, value)
        updateCameraThread()
    }

    fun updateSpeed(value: Long){
        captureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, value)
        updateCameraThread()
    }

    fun takePicture() {
        if (cameraDevice == null) {
            listener.onError("CameraPro.captureStillPicture::cameraDevice is null")
            return
        }
        try {
            val captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(imageReader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)

            cameraCaptureSession.stopRepeating()
            cameraCaptureSession.abortCaptures()
            cameraCaptureSession.capture(captureBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                    cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                }
            }, null)
        } catch (e: CameraAccessException) {
            listener.onError("CameraPro.takePicture.CameraAccessException::${e.message}", true)
        }
    }
}