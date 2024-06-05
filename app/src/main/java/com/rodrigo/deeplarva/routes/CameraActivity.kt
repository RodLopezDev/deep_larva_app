package com.rodrigo.deeplarva.routes

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.MeteringRectangle
import android.media.Image
import android.media.ImageReader
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.Range
import android.util.Size
import android.view.*
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.routes.view.CameraView
import com.rodrigo.deeplarva.ui.camera.Camera
import com.rodrigo.deeplarva.ui.camera.CameraEventsListener
import com.rodrigo.deeplarva.ui.camera.CameraParameters
import com.rodrigo.deeplarva.ui.camera.CameraSurfaceTextureListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt
import kotlinx.coroutines.*

import java.time.Duration
class CameraActivity: AppCompatActivity(), CameraEventsListener {
    private val TAG = "Rodrigo"

    // CONSTANTS
    private val REQUEST_CAMERA_PERMISSION = 1

    // CAMERA FEATURES
//    private var imageDimension: Size? = null
//    private var exposureRange: Range<Int>? = null
//    private var exposureStep: Float = 0f
//    private var isoRange: Range<Int>? = null
//    private var speedRange: Range<Long>? = null

    private lateinit var imageReader: ImageReader

    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null

    private var cameraDevice: CameraDevice? = null
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    private var currentZoomLevel = 1f
    private var maxZoomLevel = 1f

    companion object {
        const val PREFS_NAME = "CameraPrefs"
        const val PREF_ISO = "pref_iso"
        const val PREF_SPEED = "pref_speed"
        const val PREF_EV = "pref_ev"
    }

    private var cameraParameters = CameraParameters()

    private lateinit var view: CameraView
    private lateinit var surfaceListener: CameraSurfaceTextureListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        surfaceListener = CameraSurfaceTextureListener(this)

        view = CameraView(this, cameraParameters)
        view.textureView.surfaceTextureListener = surfaceListener


//        scaleGestureDetector = ScaleGestureDetector(
//            this,
//            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
//                override fun onScale(detector: ScaleGestureDetector): Boolean {
//                    cameraDevice?.let {
//                        val scaleFactor = detector.scaleFactor
//                        currentZoomLevel =
//                            (currentZoomLevel * scaleFactor).coerceIn(1f, maxZoomLevel)
//                        val characteristics =
//                            (getSystemService(Context.CAMERA_SERVICE) as CameraManager)
//                                .getCameraCharacteristics(it.id)
//                        val maxZoom =
//                            characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
//                                ?: 1f
//                        maxZoomLevel = maxZoom
//                        val rect =
//                            characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
//                                ?: return false
//                        val cropWidth = (rect.width() / currentZoomLevel).toInt()
//                        val cropHeight = (rect.height() / currentZoomLevel).toInt()
//                        val cropRect = Rect(
//                            rect.centerX() - cropWidth / 2,
//                            rect.centerY() - cropHeight / 2,
//                            rect.centerX() + cropWidth / 2,
//                            rect.centerY() + cropHeight / 2
//                        )
//                        captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, cropRect)
//                        updatePreview()
//                    }
//                    return true
//                }
//            })
//
//        textureView.setOnTouchListener { _, event ->
//            scaleGestureDetector.onTouchEvent(event)
//            if (event.action == MotionEvent.ACTION_DOWN) {
//                focusOnTouch(event.x, event.y)
//            }
//            true
//        }
    }

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        val image: Image? = reader.acquireLatestImage()
        image?.let {
            val buffer: ByteBuffer = it.planes[0].buffer
            val bytes = ByteArray(buffer.capacity())
            buffer.get(bytes)
            saveImage(bytes)
            it.close()
        }
    }

    private fun saveImage(bytes: ByteArray) {
        val imageFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyAppImages")
        if (!imageFolder.exists()) {
            imageFolder.mkdirs()
        }

        val fileName = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date()) + ".jpg"
        val imageFile = File(imageFolder, fileName)

        try {
            FileOutputStream(imageFile).use { output ->
                output.write(bytes)
                Toast.makeText(applicationContext, "Saved: $imageFile", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
            cameraDevice = null
            finish()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice?.close()
            cameraDevice = null
            finish()
        }
    }
    private fun createCameraPreview() {
        try {
            val texture = view.textureView.surfaceTexture!!
            val previewSize = cameraParameters.imageDimension ?: return

            view.textureView.post {
                adjustAspectRatio(view.textureView, previewSize.width, previewSize.height)
            }

            texture.setDefaultBufferSize(previewSize.width, previewSize.height)

            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF)

            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val defaultISO = cameraParameters.isoRange?.lower ?: 50
            val defaultSpeed = cameraParameters.speedRange?.upper ?: 100000000L
            val defaultEV = 0

            val savedISO = prefs.getInt(PREF_ISO, defaultISO)
            val savedSpeed = prefs.getLong(PREF_SPEED, defaultSpeed)
            val savedEV = prefs.getInt(PREF_EV, defaultEV)

            captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, savedISO)
            captureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, savedSpeed)
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, savedEV)

//            isoSeekBar.progress = (savedISO - (isoRange?.lower ?: 0)) * 100 / ((isoRange?.upper ?: 100) - (isoRange?.lower ?: 0))
            val speedProgress = ((savedSpeed - (cameraParameters.speedRange?.lower ?: 0L)) * 100 / ((cameraParameters.speedRange?.upper ?: 100000000L) - (cameraParameters.speedRange?.lower ?: 0L))) +1
//            speedSeekBar.progress = speedProgress.toInt()
//            exposureSeekBar.progress = (savedEV - (exposureRange?.lower ?: 0)) * 100 / ((exposureRange?.upper ?: 0) - (exposureRange?.lower ?: 0))

            cameraDevice!!.createCaptureSession(listOf(surface, imageReader.surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    if (cameraDevice == null) return
                    captureSession = cameraCaptureSession
                    updatePreview()
                }

                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    Toast.makeText(this@CameraActivity, "Configuration change", Toast.LENGTH_SHORT).show()
                }
            }, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating camera preview: ${e.message}")
        }
    }

    private fun updatePreview() {
        if (cameraDevice == null) return
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF)
        try {
            captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating preview: ${e.message}")
        }
    }

    private fun adjustAspectRatio(textureView: TextureView, previewWidth: Int, previewHeight: Int) {
        val viewWidth = textureView.width
        val newWidth = viewWidth
        val newHeight = ((viewWidth * previewWidth)/previewHeight).toInt()
        val layoutParams = textureView.layoutParams
        layoutParams.width = newWidth
        layoutParams.height = newHeight
        textureView.layoutParams = layoutParams
    }

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            initCamera()
        }
        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {}

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
            return true
        }
    }

    override fun onResume() {
        super.onResume()
        backgroundThread = HandlerThread("Camera Background").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)

        if (view.textureView.isAvailable) {
            initCamera()
        } else {
            view.textureView.surfaceTextureListener = surfaceListener
        }
    }

    private fun focusOnTouch(x: Float, y: Float) {
        cameraDevice?.let {
            val characteristics = (getSystemService(Context.CAMERA_SERVICE) as CameraManager)
                .getCameraCharacteristics(it.id)
            val sensorArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?: return

            val focusAreaSize = 200
            val focusArea = Rect(
                (x / view.textureView.width * sensorArraySize.width()).toInt() - focusAreaSize / 2,
                (y / view.textureView.height * sensorArraySize.height()).toInt() - focusAreaSize / 2,
                (x / view.textureView.width * sensorArraySize.width()).toInt() + focusAreaSize / 2,
                (y / view.textureView.height * sensorArraySize.height()).toInt() + focusAreaSize / 2
            )

            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, arrayOf(MeteringRectangle(focusArea, MeteringRectangle.METERING_WEIGHT_MAX)))
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)

            captureSession.capture(captureRequestBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE)
                    updatePreview()
                }
            }, backgroundHandler)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCamera()
            } else {
                Toast.makeText(applicationContext, "You need to grant camera permission to use this app", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    private fun takePhoto() {
        try {
            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(imageReader.surface)
                set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, captureRequestBuilder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION) ?: 0)
                set(CaptureRequest.SENSOR_SENSITIVITY, captureRequestBuilder.get(CaptureRequest.SENSOR_SENSITIVITY) ?: 100)
                set(CaptureRequest.SENSOR_EXPOSURE_TIME, captureRequestBuilder.get(CaptureRequest.SENSOR_EXPOSURE_TIME) ?: 100000000L)
                set(CaptureRequest.JPEG_ORIENTATION, 90)
            }

            captureSession.capture(captureBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    Toast.makeText(this@CameraActivity, "Photo Captured", Toast.LENGTH_SHORT).show()

                    // Obtener el URI de la imagen capturada
                    val uri = saveImageToGallery()  // Necesitas implementar esta función
                    if (uri != null) {
//                        var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
//                        val bitmap = BitmapFactory.decodeFile(uri.toString()) //(contentResolver, )
                        try {
                            var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                            Log.d("PRUEBAS", "PRUEBAS")

                            // Llama a la función iniciarProcesoGlobalPrediction con los parámetros necesarios
//                            iniciarProcesoGlobalPrediction(
//                                tflite_model = "yolov8",
//                                bitmap,
//                                splitWidth = 640,
//                                splitHeight = 640,
//                                overlap = 0.75f,
//                                miCustomConfidenceThreshold = 0.28F,
//                                miCustomIoUThreshold = 0.80F,
//                                distanceThreshold = 10.0f)
//                    imageView.setImageBitmap(bitmap)
                        } catch (e: IOException) {
                            // Maneja la excepción si hay un problema al cargar la imagen desde la galería
                            e.printStackTrace()
                            //Toast.makeText(this, "Error al ejecutar iniciarProcesoGlobalPrediction sobre la imagen desde la galería", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCaptureFailed(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    failure: CaptureFailure
                ) {
                    super.onCaptureFailed(session, request, failure)
                    Toast.makeText(this@CameraActivity, "Photo Capture Failed", Toast.LENGTH_SHORT).show()
                }
            }, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "Error taking photo: ${e.message}")
        }
    }

    private fun saveImageToGallery(): Uri? {
        val imageFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyAppImages")
        if (!imageFolder.exists()) {
            imageFolder.mkdirs()
        }

        val fileName = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date()) + ".jpg"
        val imageFile = File(imageFolder, fileName)

        return try {
            val fos = FileOutputStream(imageFile)
            imageReader.acquireLatestImage().use { image ->
                if(image == null) return@use
                val buffer: ByteBuffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.capacity())
                buffer.get(bytes)
                fos.write(bytes)
                fos.close()
            }

            val uri = Uri.fromFile(imageFile)
//            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
//            mediaScanIntent.data = uri
//            sendBroadcast(mediaScanIntent)

            MediaScannerConnection.scanFile(applicationContext,
                arrayOf(uri.toString()),
                null, null);

            uri
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun initCamera() {
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@CameraActivity, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            return
        }
        try {
            val camera = Camera(this)
            imageReader = ImageReader.newInstance(camera.largest.width, camera.largest.height, ImageFormat.JPEG, 3)
            imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)

            cameraParameters.update(camera)
            camera.openCamera(stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera: ${e.message}")
        }
    }
}