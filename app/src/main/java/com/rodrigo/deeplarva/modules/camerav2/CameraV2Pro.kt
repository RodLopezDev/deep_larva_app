package com.rodrigo.deeplarva.modules.camerav2

import android.Manifest
import android.util.DisplayMetrics
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraV2Pro(private val activity: AppCompatActivity, private val listener: ICameraV2ProListener) {
    companion object{
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private val REQUEST_CODE_PERMISSIONS = 10

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    private val lensFacing: Int = CameraSelector.LENS_FACING_BACK

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private lateinit var camera: Camera
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun startCamera() {
        val cameraProviderFinally = ProcessCameraProvider.getInstance(activity)
        cameraProviderFinally.addListener(Runnable {
            cameraProvider = cameraProviderFinally.get()
            bindCamera()
        }, ContextCompat.getMainExecutor(activity))
    }

    fun offCamera() {
        cameraExecutor.shutdown()
    }

    private fun bindCamera(isoPreDefined: Int? = null){
        val metrics = DisplayMetrics().also { listener.getPreviewView().display.getRealMetrics(it) }
        val screenAspectRatio = aspectRadio(metrics.widthPixels, metrics.heightPixels)
        val rotation = listener.getPreviewView().display.rotation

        val cameraProvider = cameraProvider ?: throw IllegalStateException("Fallo al iniciar la camara")

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()


        cameraProvider.unbindAll()

        try{
            camera = cameraProvider.bindToLifecycle(activity, cameraSelector, preview, imageCapture)
            camera.cameraInfo.exposureState.exposureCompensationRange
            updateISO(isoPreDefined)
        }catch(exc: Exception){
            Log.e("CameraWildRunning", "Fallo al vincular la camara", exc)
        }
    }
    private fun aspectRadio(width: Int, height: Int): Int{
        val previewRatio = max(width, height).toDouble() / min(width, height)

        if (abs(previewRatio - CameraV2Pro.RATIO_4_3_VALUE) <= abs(previewRatio - CameraV2Pro.RATIO_16_9_VALUE)){
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9


    }
    private fun updateISO(isoPreDefined: Int? = null) {
        if(camera == null || preview == null) return
        val exposureState = camera.cameraInfo.exposureState
        if (exposureState.isExposureCompensationSupported) {
            val range = exposureState.exposureCompensationRange
            if (isoPreDefined != null && range.contains(isoPreDefined)){
                camera.cameraControl.setExposureCompensationIndex(isoPreDefined)
            }
        }
        preview?.setSurfaceProvider(listener.getPreviewView().surfaceProvider)
    }
}