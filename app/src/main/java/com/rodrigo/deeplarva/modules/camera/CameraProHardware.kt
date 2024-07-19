package com.rodrigo.deeplarva.modules.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.media.ImageReader
import android.util.Size
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.rodrigo.deeplarva.domain.view.CameraValues

class CameraProHardware(
    private val activity: AppCompatActivity,
    private val textureView: TextureView,
    private val cameraValues: CameraValues,
    private val sensorOrientation: Int,
    private val windowRotation: Int,
    private val listener: CameraProHardwareListener
) {
    private lateinit var imageReader: ImageReader
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var cameraCaptureSession: CameraCaptureSession

    private var currentZoomLevel = 1f
    private var maxZoomLevel = 1f
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    fun onStart() {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                openCamera()
            }
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
        }

        scaleGestureDetector = ScaleGestureDetector(activity, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                cameraDevice?.let {
                    val scaleFactor = detector.scaleFactor
                    currentZoomLevel = (currentZoomLevel * scaleFactor).coerceIn(1f, maxZoomLevel)
                    val characteristics = (activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager)
                        .getCameraCharacteristics(it.id)
                    val maxZoom = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1f
                    maxZoomLevel = maxZoom
                    val rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?: return false
                    val cropWidth = (rect.width() / currentZoomLevel).toInt()
                    val cropHeight = (rect.height() / currentZoomLevel).toInt()
                    val cropRect = Rect(
                        rect.centerX() - cropWidth / 2,
                        rect.centerY() - cropHeight / 2,
                        rect.centerX() + cropWidth / 2,
                        rect.centerY() + cropHeight / 2
                    )
                    captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, cropRect)
                    updateCameraThread()
                }
                return true
            }
        })

        textureView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
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
        configureTransform(textureView)
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
            listener.onReceivePicture(image, sensorOrientation, windowRotation)
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

    private fun configureTransform(view: TextureView) {
        val viewSize = Size(textureView.width, textureView.height)

        val rotateDegrees = when (windowRotation) {
            Surface.ROTATION_0 -> sensorOrientation - 90
            Surface.ROTATION_90 -> sensorOrientation - 180
            Surface.ROTATION_180 -> sensorOrientation - 270
            Surface.ROTATION_270 -> sensorOrientation
            else -> sensorOrientation
        }

        val validHeight = if (windowRotation == Surface.ROTATION_0) viewSize.width  else viewSize.height // Modify aspect ratio of TextureView in vertical
        val validWidth = if (windowRotation == Surface.ROTATION_180) viewSize.height  else viewSize.width // Modify aspect ratio of TextureView in vertical inverted

        val matrix = Matrix()
        val viewRect = android.graphics.RectF(0f, 0f, viewSize.width.toFloat(), validHeight.toFloat())
        val bufferRect = android.graphics.RectF(0f, 0f, viewSize.height.toFloat(), validWidth.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()

        bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
        matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
        matrix.postRotate(rotateDegrees.toFloat(), centerX, centerY)

        view.setTransform(matrix)
    }
}