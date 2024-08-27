package com.rodrigo.deeplarva.routes.activity

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture
import com.kylecorry.andromeda.camera.ImageCaptureSettings
import com.kylecorry.andromeda.core.time.CoroutineTimer
import com.kylecorry.andromeda.core.ui.setOnProgressChangeListener
import com.kylecorry.andromeda.files.ExternalFileSystem
import com.kylecorry.andromeda.files.LocalFileSystem
import com.kylecorry.andromeda.haptics.HapticFeedbackType
import com.kylecorry.andromeda.haptics.HapticMotor
import com.kylecorry.andromeda.pickers.Pickers
import com.kylecorry.luna.coroutines.CoroutineQueueRunner
import com.kylecorry.luna.coroutines.onIO
import com.kylecorry.luna.coroutines.onMain
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.databinding.ActivityCameraPro2Binding
import com.rodrigo.deeplarva.helpers.PreferencesHelper
import com.rodrigo.deeplarva.modules.camerapro2.infraestructure.SensitivityProvider
import com.rodrigo.deeplarva.modules.camerapro2.ui.CustomUiUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.time.Duration

class CameraProV2Activity: AppCompatActivity() {
    private var deviceID: String = ""
    private val pictures = mutableListOf<String>()
    private lateinit var binding: ActivityCameraPro2Binding
    private lateinit var files: LocalFileSystem
    private lateinit var haptics: HapticMotor

    // Require state
    private var focusPercent:Float? = null
    private var iso: Int? = null
    private var sensitivities = emptyList<Int>()
    private var cameraStartCounter = 0
    private var isCameraRunning = false
    private var isCapturing = false
    private var interval: Duration? = null
    private var shutterSpeed: Duration? = null
    private var zoomRatio = 1f

    private val queue = CoroutineQueueRunner(1)

    private var hasPendingPhoto = false
    private var turnOffDuringInterval = false

    private val intervalometer = CoroutineTimer {
        if (turnOffDuringInterval) {
            println("INTERVAL")
            hasPendingPhoto = true
            onMain {
                restartCamera()
            }
        } else {
            takePhoto()
        }
    }

    private val delayedPhotoTimer = CoroutineTimer {
        takePhoto()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceID = PreferencesHelper(this).getString(Constants.SHARED_PREFERENCES_DEVICE_ID) ?: ""

        binding = ActivityCameraPro2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        files = LocalFileSystem(this)
        haptics = HapticMotor(this)

        onCreatedView()
    }
    @OptIn(ExperimentalZeroShutterLag::class)
    override fun onResume() {
        super.onResume()
        binding.camera.setOnReadyListener {
            val camera = binding.camera.camera ?: return@setOnReadyListener
            val sensitivityProvider = SensitivityProvider()
            sensitivities = sensitivityProvider.getValues(camera)
            cameraStartCounter++

            if (hasPendingPhoto) {
                delayedPhotoTimer.once(Duration.ofMillis(500))
            }
        }
        startCamera()
    }


    override fun onPause() {
        super.onPause()
        intervalometer.stop()
        delayedPhotoTimer.stop()
        haptics.off()
        stopCamera()
        isCapturing = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onCreatedView () {

        binding.focus.setOnProgressChangeListener { progress, _ ->
            focusPercent = progress / 100f
        }

        binding.captureButton.setOnClickListener {
            takePhoto()
        }

        binding.iso.setOnClickListener {
            val sensitivityNames =
                listOf("Auto") + sensitivities.map { it.toString() }

            Pickers.item(
                applicationContext,
                "Iso",
                sensitivityNames,
                sensitivities.indexOf(iso) + 1,
            ) {
                it ?: return@item
                iso = if (it == 0) {
                    null
                } else {
                    sensitivities[it - 1]
                }
            }
        }

        binding.shutterSpeed.setOnClickListener {
            Pickers.number(
                applicationContext,
                "Shutter Speed",
                default = shutterSpeed?.toMillis()?.div(1000f),
                allowDecimals = true,
                allowNegative = false,
                hint = "Shutter Speed"
            ) {
                if (it != null) {
                    shutterSpeed = Duration.ofMillis((it.toFloat() * 1000).toLong())
                }
            }
        }

        binding.interval.setOnClickListener {
            CustomUiUtils.pickDuration(
                applicationContext,
                interval,
                "Interval",
                showSeconds = true
            ) {
                interval = it
            }
        }

        binding.camera.setOnZoomChangeListener {
            zoomRatio = binding.camera.camera?.zoom?.ratio ?: 1f
        }
    }

    private fun startCamera() {
        binding.camera.start(
            readFrames = false,
            captureSettings = ImageCaptureSettings(
                quality = 100,
                captureMode = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY,
                rotation = windowManager.defaultDisplay.rotation
            )
        )
        isCameraRunning = true
    }

    private fun restartCamera() {
        stopCamera()
        startCamera()
    }

    private fun stopCamera() {
        binding.camera.stop()
        isCameraRunning = false
    }

    private fun takePhoto() {
        GlobalScope.launch {
            queue.enqueue {
                val fileName = "${deviceID}-${System.currentTimeMillis()}"
                val imageFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/deep-larva/")
                if (!imageFolder.exists()) {
                    imageFolder.mkdirs()
                }
                val file = File(imageFolder, "$fileName${Constants.IMAGE_EXTENSION}")

                isCapturing = true

                onIO {
                    binding.camera.capture(file)
                    pictures.add(file.absolutePath)
                    onCloseView()
                }

                haptics.feedback(HapticFeedbackType.Click)

                isCapturing = false
                hasPendingPhoto = false
                if (turnOffDuringInterval) {
                    onMain {
                        stopCamera()
                    }
                }
            }
        }
    }

    private suspend fun copyToMediaStore(file: File) = onIO {
        // Save the file to the system media store
        val resolver = applicationContext.contentResolver
        val photoCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val photoDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val photoContentUri = resolver.insert(photoCollection, photoDetails) ?: return@onIO
        val externalFiles = ExternalFileSystem(applicationContext)
        externalFiles.outputStream(photoContentUri)?.use { output ->
            file.inputStream().use { input ->
                input.copyTo(output)
            }
        }

        photoDetails.clear()
        photoDetails.put(MediaStore.Audio.Media.IS_PENDING, 0)
        resolver.update(photoContentUri, photoDetails, null, null)

        pictures.add(file.absolutePath)
        onCloseView()
    }
    private fun onCloseView() {
        val returnIntent = Intent()
        if(pictures.isEmpty()) {
            setResult(RESULT_CANCELED, returnIntent)
        } else {
            val intentData = pictures.joinToString(",,,")
            returnIntent.putExtra(Constants.INTENT_CAMERA_PRO_RESULT, intentData)
            setResult(RESULT_OK, returnIntent)
        }
        finish()
    }
}