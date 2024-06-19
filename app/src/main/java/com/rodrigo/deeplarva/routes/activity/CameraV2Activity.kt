package com.rodrigo.deeplarva.routes.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.google.android.material.snackbar.Snackbar
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.routes.activity.view.CameraV2ActivityView
import com.rodrigo.deeplarva.routes.activity.view.ICameraV2ViewListener
import java.io.File
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraV2Activity: AppCompatActivity() {
    companion object{
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private val REQUEST_CODE_PERMISSIONS = 10

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    private var FILENAME : String = ""

    private var preview: Preview? = null

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var cameraProvider: ProcessCameraProvider? = null

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File

    private lateinit var cameraExecutor: ExecutorService


    private lateinit var dateRun: String
    private lateinit var startTimeRun: String

    private lateinit var camera: Camera


    private lateinit var view: CameraV2ActivityView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = CameraV2ActivityView(this, object: ICameraV2ViewListener {
            override fun onTakePicture() {
                takePhoto()
            }
            override fun onUpdateExposure(value: Int) {
                updateISO(value)
            }
            override fun getMinExposure(): Int {
                return -20
            }
            override fun getMaxExposure(): Int {
                return 20
            }
            override fun getDefaultExposure(): Int {
                return 0
            }
        })

        val bundle = intent.extras
        dateRun = bundle?.getString("dateRun").toString()
        startTimeRun = bundle?.getString("startTimeRun").toString()

        cameraExecutor = Executors.newSingleThreadExecutor()

        outputDirectory = getOutputDirectory()
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) startCamera()
        else ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS){
            if (allPermissionsGranted()) startCamera()
            else{
                Toast.makeText(this, "Debes proporcionar permisos si quieres tomar fotos", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    private fun bindCamera(isoPreDefined: Int? = null){
        val metrics = DisplayMetrics().also { view.getPreview().display.getRealMetrics(it) }
        val screenAspectRatio = aspectRadio(metrics.widthPixels, metrics.heightPixels)
        val rotation = view.getPreview().display.rotation

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
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            camera.cameraInfo.exposureState.exposureCompensationRange
            updateISO(isoPreDefined)
            //preview?.setSurfaceProvider(view.getPreview().surfaceProvider)
        }catch(exc: Exception){
            Log.e("CameraWildRunning", "Fallo al vincular la camara", exc)
        }


    }
    private fun aspectRadio(width: Int, height: Int): Int{
        val previewRatio = max(width, height).toDouble() / min(width, height)

        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)){
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9


    }
    private fun startCamera(){
        val cameraProviderFinally = ProcessCameraProvider.getInstance(this)
        cameraProviderFinally.addListener(Runnable {

            cameraProvider = cameraProviderFinally.get()

            lensFacing = when{
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("No tenemos camara")
            }

//            manageSwitchButton()

            bindCamera()

        }, ContextCompat.getMainExecutor(this))


    }

    private fun hasBackCamera(): Boolean{
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }
    private fun hasFrontCamera(): Boolean{
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    private fun getOutputDirectory(): File{
        val mediaDir = externalMediaDirs.firstOrNull()?.let{
            File(it, "wildRunning").apply {  mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir

    }
    private fun takePhoto(){
        FILENAME = "demo-" + UUID.randomUUID()
        FILENAME = FILENAME.replace(":", "")
        FILENAME = FILENAME.replace("/", "")

        val photoFile = File (outputDirectory, FILENAME + ".jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object:ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    val  savedUri = Uri.fromFile(photoFile)

//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//                        setGalleryThumbnail (savedUri)
//                    }

                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(savedUri.toFile().extension)
                    MediaScannerConnection.scanFile(
                        baseContext,
                        arrayOf(savedUri.toFile().absolutePath),
                        arrayOf(mimeType)
                    ){ _, uri ->

                    }

                    var clMain = findViewById<ConstraintLayout>(R.id.clMain)
                    Snackbar.make(clMain, "Imagen guardada con éxito", Snackbar.LENGTH_LONG).setAction("OK"){
                        clMain.setBackgroundColor(Color.CYAN)
                    }.show()
                }

                override fun onError(exception: ImageCaptureException) {
                    var clMain = findViewById<ConstraintLayout>(R.id.clMain)
                    Snackbar.make(clMain, "Error al guardar la imagen", Snackbar.LENGTH_LONG).setAction("OK"){
                        clMain.setBackgroundColor(Color.CYAN)
                    }.show()
                }
            })
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

        preview?.setSurfaceProvider(view.getPreview().surfaceProvider)
    }

//    private fun setGalleryThumbnail(uri: Uri){
//        var thumbnail = binding.photoViewButton
//        thumbnail.post {
//            Glide.with (thumbnail)
//                .load(uri)
//                .apply(RequestOptions.circleCropTransform())
//                .into(thumbnail)
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}