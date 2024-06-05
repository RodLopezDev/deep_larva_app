package com.rodrigo.deeplarva.routes

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Range
import android.util.Size
import android.view.ScaleGestureDetector
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.databinding.ActivityCameraBinding

class CameraActivity: AppCompatActivity() {
    private val TAG = "Rodrigo"
    private lateinit var binding: ActivityCameraBinding

    // CONSTANTS
    private val REQUEST_CAMERA_PERMISSION = 1

    // CAMERA FEATURES
    private var imageDimension: Size? = null
    private var exposureRange: Range<Int>? = null
    private var exposureStep: Float = 0f
    private var isoRange: Range<Int>? = null
    private var speedRange: Range<Long>? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        binding = ActivityCameraBinding.inflate(layoutInflater)

        val textureView = binding.textureView
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: android.graphics.SurfaceTexture,
                width: Int,
                height: Int
            ) {
                startCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                surfaceTexture: android.graphics.SurfaceTexture,
                width: Int,
                height: Int
            ) {
                Log.d("test", "onSurfaceTextureSizeChanged")
                Log.d("test", "onSurfaceTextureSizeChanged")
            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: android.graphics.SurfaceTexture): Boolean {
                Log.d("test", "onSurfaceTextureDestroyed")
                return true
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: android.graphics.SurfaceTexture) {
                Log.d("test", "onSurfaceTextureUpdated")
            }
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(applicationContext)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider { request ->
                        val surface = android.view.Surface(binding.textureView.surfaceTexture)
                        request.provideSurface(surface, ContextCompat.getMainExecutor(applicationContext)) { result ->
                            // Handle surface result here if needed
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this@CameraActivity, cameraSelector, preview)
            } catch (exc: Exception) {
                Log.e("CameraXApp", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(applicationContext))
    }
}



//        binding.textureView.surfaceTextureListener  = surfaceTextureListener
//
//        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        binding.switchButton.isChecked = prefs.getBoolean("SHOW_CONTROLS", false)
//        setControlVisibility(binding.switchButton.isChecked)
//
//        binding.exposureSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                updateExposure(progress)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })
//
//        binding.isoSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                updateISO(progress)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })
//
//        binding.speedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                updateSpeed(progress)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })
//
//        binding.captureButton.setOnClickListener {
//            //takeMultiplePhotos(3, 1000)
//            takePhoto()
//        }
//
//        binding.switchButton.setOnCheckedChangeListener { _, isChecked ->
//            setControlVisibility(isChecked)
//        }
//
//        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
//            override fun onScale(detector: ScaleGestureDetector): Boolean {
//                cameraDevice?.let {
//                    val scaleFactor = detector.scaleFactor
//                    currentZoomLevel = (currentZoomLevel * scaleFactor).coerceIn(1f, maxZoomLevel)
//                    val characteristics = (getSystemService(Context.CAMERA_SERVICE) as CameraManager)
//                        .getCameraCharacteristics(it.id)
//                    val maxZoom = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1f
//                    maxZoomLevel = maxZoom
//                    val rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?: return false
//                    val cropWidth = (rect.width() / currentZoomLevel).toInt()
//                    val cropHeight = (rect.height() / currentZoomLevel).toInt()
//                    val cropRect = Rect(
//                        rect.centerX() - cropWidth / 2,
//                        rect.centerY() - cropHeight / 2,
//                        rect.centerX() + cropWidth / 2,
//                        rect.centerY() + cropHeight / 2
//                    )
//                    captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, cropRect)
//                    updatePreview()
//                }
//                return true
//            }
//        })
//
//        binding.textureView.setOnTouchListener { _, event ->
//            scaleGestureDetector.onTouchEvent(event)
//            if (event.action == MotionEvent.ACTION_DOWN) {
//                focusOnTouch(event.x, event.y)
//            }
//            true
//        }


//    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
//        val image: Image? = reader.acquireLatestImage()
//        image?.let {
//            val buffer: ByteBuffer = it.planes[0].buffer
//            val bytes = ByteArray(buffer.capacity())
//            buffer.get(bytes)
//            saveImage(bytes)
//            it.close()
//        }
//    }
//
//    private fun saveImage(bytes: ByteArray) {
//        val imageFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyAppImages")
//        if (!imageFolder.exists()) {
//            imageFolder.mkdirs()
//        }
//
//        val fileName = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date()) + ".jpg"
//        val imageFile = File(imageFolder, fileName)
//
//        try {
//            FileOutputStream(imageFile).use { output ->
//                output.write(bytes)
//                Toast.makeText(applicationContext, "Saved: $imageFile", Toast.LENGTH_SHORT).show()
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//            Toast.makeText(applicationContext, "Failed to save image", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private val stateCallback = object : CameraDevice.StateCallback() {
//        override fun onOpened(camera: CameraDevice) {
//            cameraDevice = camera
//            createCameraPreview()
//        }
//
//        override fun onDisconnected(camera: CameraDevice) {
//            cameraDevice?.close()
//            cameraDevice = null
//            finish()
//        }
//
//        override fun onError(camera: CameraDevice, error: Int) {
//            cameraDevice?.close()
//            cameraDevice = null
//            finish()
//        }
//    }
//    private fun createCameraPreview() {
//        try {
//            if(binding.textureView.surfaceTexture == null) {
//                binding.textureView.surfaceTexture = surfaceTextureListener
//            }
//
//            val texture = binding.textureView.surfaceTexture!!
//            val previewSize = imageDimension ?: return
//
//            binding.textureView.post {
//                adjustAspectRatio(binding.textureView, previewSize.width, previewSize.height)
//            }
//
//            texture.setDefaultBufferSize(previewSize.width, previewSize.height)
//
//            val surface = Surface(texture)
//            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
//            captureRequestBuilder.addTarget(surface)
//            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF)
//
//            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//            val defaultISO = isoRange?.lower ?: 50
//            val defaultSpeed = speedRange?.upper ?: 100000000L
//            val defaultEV = 0
//
//            val savedISO = prefs.getInt(PREF_ISO, defaultISO)
//            val savedSpeed = prefs.getLong(PREF_SPEED, defaultSpeed)
//            val savedEV = prefs.getInt(PREF_EV, defaultEV)
//
//            captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, savedISO)
//            captureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, savedSpeed)
//            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, savedEV)
//
////            isoSeekBar.progress = (savedISO - (isoRange?.lower ?: 0)) * 100 / ((isoRange?.upper ?: 100) - (isoRange?.lower ?: 0))
//            val speedProgress = ((savedSpeed - (speedRange?.lower ?: 0L)) * 100 / ((speedRange?.upper ?: 100000000L) - (speedRange?.lower ?: 0L))) +1
////            speedSeekBar.progress = speedProgress.toInt()
////            exposureSeekBar.progress = (savedEV - (exposureRange?.lower ?: 0)) * 100 / ((exposureRange?.upper ?: 0) - (exposureRange?.lower ?: 0))
//
//            cameraDevice!!.createCaptureSession(listOf(surface, imageReader.surface), object : CameraCaptureSession.StateCallback() {
//                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
//                    if (cameraDevice == null) return
//                    captureSession = cameraCaptureSession
//                    updatePreview()
//                }
//
//                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
//                    Toast.makeText(this@CameraActivity, "Configuration change", Toast.LENGTH_SHORT).show()
//                }
//            }, backgroundHandler)
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error creating camera preview: ${e.message}")
//        }
//    }
//
//    private fun updatePreview() {
//        if (cameraDevice == null) return
//        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF)
//        try {
//            captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler)
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error updating preview: ${e.message}")
//        }
//    }
//
//    private fun adjustAspectRatio(textureView: TextureView, previewWidth: Int, previewHeight: Int) {
//        val viewWidth = textureView.width
//        val newWidth = viewWidth
//        val newHeight = ((viewWidth * previewWidth)/previewHeight).toInt()
//        val layoutParams = textureView.layoutParams
//        layoutParams.width = newWidth
//        layoutParams.height = newHeight
//        textureView.layoutParams = layoutParams
//    }
//
//
//    private fun openCamera() {
//        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
//        try {
//            val cameraId = manager.cameraIdList[0]
//            val characteristics = manager.getCameraCharacteristics(cameraId)
//            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
//
//            val largest = map!!.getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.width * it.height }!!
//
//            imageDimension = largest
//
//            exposureRange = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
//            exposureStep = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)?.toFloat() ?: 0f
//            isoRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
//            speedRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
//
//            imageReader = ImageReader.newInstance(largest.width, largest.height, ImageFormat.JPEG, 3)
//            imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
//
//            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this@CameraActivity, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
//                return
//            }
//            manager.openCamera(cameraId, stateCallback, backgroundHandler)
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error opening camera: ${e.message}")
//        }
//    }
//
//    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
//        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
//            openCamera()
//        }
//        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {}
//
//        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
//
//        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
//            return true
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        backgroundThread = HandlerThread("Camera Background").also { it.start() }
//        backgroundHandler = Handler(backgroundThread!!.looper)
//
//
//        if (binding.textureView.isAvailable) {
//            openCamera()
//        } else {
//            binding.textureView.surfaceTextureListener = surfaceTextureListener
//            openCamera()
//        }
//    }
//
//    private fun focusOnTouch(x: Float, y: Float) {
//        cameraDevice?.let {
//            val characteristics = (getSystemService(Context.CAMERA_SERVICE) as CameraManager)
//                .getCameraCharacteristics(it.id)
//            val sensorArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?: return
//
//            val focusAreaSize = 200
//            val focusArea = Rect(
//                (x / binding.textureView.width * sensorArraySize.width()).toInt() - focusAreaSize / 2,
//                (y / binding.textureView.height * sensorArraySize.height()).toInt() - focusAreaSize / 2,
//                (x / binding.textureView.width * sensorArraySize.width()).toInt() + focusAreaSize / 2,
//                (y / binding.textureView.height * sensorArraySize.height()).toInt() + focusAreaSize / 2
//            )
//
//            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, arrayOf(MeteringRectangle(focusArea, MeteringRectangle.METERING_WEIGHT_MAX)))
//            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
//            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
//
//            captureSession.capture(captureRequestBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
//                override fun onCaptureCompleted(
//                    session: CameraCaptureSession,
//                    request: CaptureRequest,
//                    result: TotalCaptureResult
//                ) {
//                    super.onCaptureCompleted(session, request, result)
//                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE)
//                    updatePreview()
//                }
//            }, backgroundHandler)
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                openCamera()
//            } else {
//                Toast.makeText(applicationContext, "You need to grant camera permission to use this app", Toast.LENGTH_SHORT).show()
//                finish()
//            }
//        }
//    }
//
//    private fun setControlVisibility(isVisible: Boolean) {
//        val visibility = if (isVisible) SeekBar.VISIBLE else SeekBar.GONE
//        binding.exposureSeekBar.visibility = visibility
//        binding.exposureValueText.visibility = visibility
//        binding.isoSeekBar.visibility = visibility
//        binding.isoValueText.visibility = visibility
//        binding.speedSeekBar.visibility = visibility
//        binding.speedValueText.visibility = visibility
//    }
//
//    private fun updateExposure(progress: Int) {
//        cameraDevice?.let {
//            val minExposure = exposureRange?.lower ?: 0
//            val maxExposure = exposureRange?.upper ?: 0
//            val exposureCompensation = minExposure + (progress * (maxExposure - minExposure) / 100)
//            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, exposureCompensation)
//            val exposureValue = exposureCompensation * exposureStep
//            binding.exposureValueText.text = "EV: %.1f".format(exposureValue)
//            updatePreview()
//        }
//    }
//
//    private fun updateISO(progress: Int) {
//        cameraDevice?.let {
//            val minISO = isoRange?.lower ?: 0
//            val maxISO = isoRange?.upper ?: 0
//            val isoValue = minISO + (progress * (maxISO - minISO) / 100)
//            captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, isoValue)
//            binding.isoValueText.text = "ISO: $isoValue"
//            updatePreview()
//        }
//    }
//
//    private fun updateSpeed(progress: Int) {
//        cameraDevice?.let {
//            val minSpeed = speedRange?.lower ?: 0L
//            val maxSpeed = speedRange?.upper ?: 0L
//            val speedValue = minSpeed + (progress * (maxSpeed - minSpeed) / 100)
//            captureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, speedValue)
//            binding.speedValueText.text = "Speed: 1/${1000000000 / (if (speedValue <= 0) 1 else speedValue)} sec"
//            updatePreview()
//        }
//    }
//
//    private fun takePhoto() {
//        try {
//            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
//                addTarget(imageReader.surface)
//                set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, captureRequestBuilder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION) ?: 0)
//                set(CaptureRequest.SENSOR_SENSITIVITY, captureRequestBuilder.get(CaptureRequest.SENSOR_SENSITIVITY) ?: 100)
//                set(CaptureRequest.SENSOR_EXPOSURE_TIME, captureRequestBuilder.get(CaptureRequest.SENSOR_EXPOSURE_TIME) ?: 100000000L)
//                set(CaptureRequest.JPEG_ORIENTATION, 90)
//            }
//
//            captureSession.capture(captureBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
//                override fun onCaptureCompleted(
//                    session: CameraCaptureSession,
//                    request: CaptureRequest,
//                    result: TotalCaptureResult
//                ) {
//                    super.onCaptureCompleted(session, request, result)
//                    Toast.makeText(this@CameraActivity, "Photo Captured", Toast.LENGTH_SHORT).show()
//
//                    // Obtener el URI de la imagen capturada
//                    val uri = saveImageToGallery()  // Necesitas implementar esta función
//                    if (uri != null) {
//                        //bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
//                        val bitmap = BitmapFactory.decodeFile(uri.toString()) //(contentResolver, )
//                        try {
//                            //bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
//
//                            // Llama a la función iniciarProcesoGlobalPrediction con los parámetros necesarios
////                            iniciarProcesoGlobalPrediction(
////                                tflite_model = "yolov8",
////                                bitmap,
////                                splitWidth = 640,
////                                splitHeight = 640,
////                                overlap = 0.75f,
////                                miCustomConfidenceThreshold = 0.28F,
////                                miCustomIoUThreshold = 0.80F,
////                                distanceThreshold = 10.0f)
////                    imageView.setImageBitmap(bitmap)
//                        } catch (e: IOException) {
//                            // Maneja la excepción si hay un problema al cargar la imagen desde la galería
//                            e.printStackTrace()
//                            //Toast.makeText(this, "Error al ejecutar iniciarProcesoGlobalPrediction sobre la imagen desde la galería", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//
//                override fun onCaptureFailed(
//                    session: CameraCaptureSession,
//                    request: CaptureRequest,
//                    failure: CaptureFailure
//                ) {
//                    super.onCaptureFailed(session, request, failure)
//                    Toast.makeText(this@CameraActivity, "Photo Capture Failed", Toast.LENGTH_SHORT).show()
//                }
//            }, backgroundHandler)
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error taking photo: ${e.message}")
//        }
//    }
//
//    private fun saveImageToGallery(): Uri? {
//        val imageFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyAppImages")
//        if (!imageFolder.exists()) {
//            imageFolder.mkdirs()
//        }
//
//        val fileName = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date()) + ".jpg"
//        val imageFile = File(imageFolder, fileName)
//
//        return try {
//            val fos = FileOutputStream(imageFile)
//            imageReader.acquireLatestImage().use { image ->
//                if(image == null) return@use
//                val buffer: ByteBuffer = image.planes[0].buffer
//                val bytes = ByteArray(buffer.capacity())
//                buffer.get(bytes)
//                fos.write(bytes)
//                fos.close()
//            }
//
//            val uri = Uri.fromFile(imageFile)
//            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
//            mediaScanIntent.data = uri
//            sendBroadcast(mediaScanIntent)
//
//            uri
//        } catch (e: IOException) {
//            e.printStackTrace()
//            null
//        }
//    }