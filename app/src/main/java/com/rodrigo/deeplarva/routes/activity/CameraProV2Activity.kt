package com.rodrigo.deeplarva.routes.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kylecorry.andromeda.camera.ImageCaptureSettings
import com.kylecorry.andromeda.core.math.DecimalFormatter
import com.kylecorry.andromeda.core.time.CoroutineTimer
import com.kylecorry.andromeda.files.LocalFileSystem
import com.kylecorry.andromeda.haptics.HapticFeedbackType
import com.kylecorry.andromeda.haptics.HapticMotor
import com.kylecorry.luna.coroutines.CoroutineQueueRunner
import com.kylecorry.luna.coroutines.onIO
import com.kylecorry.luna.coroutines.onMain
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.databinding.ActivityCameraPro2Binding
import com.rodrigo.deeplarva.helpers.PreferencesHelper
import com.rodrigo.deeplarva.modules.camerapro2.infraestructure.SensitivityProvider
import com.rodrigo.deeplarva.routes.activity.observables.CameraV2Model
import com.rodrigo.deeplarva.routes.activity.stores.CameraParameterStore
import com.rodrigo.deeplarva.ui.widget.dialogs.SeekDialog
import com.rodrigo.deeplarva.ui.widget.dialogs.SelectableDialog
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

    private lateinit var viewModel: CameraV2Model
    private lateinit var cameraStore: CameraParameterStore

    // Require state
    private var sensitivities = emptyList<Int>()
    private var cameraStartCounter = 0
    private var isCameraRunning = false
    private var isCapturing = false
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
        cameraStore = CameraParameterStore(this)
        deviceID = PreferencesHelper(this).getString(Constants.SHARED_PREFERENCES_DEVICE_ID) ?: ""
        viewModel = ViewModelProvider(this)[CameraV2Model::class.java]

        binding = ActivityCameraPro2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        files = LocalFileSystem(this)
        haptics = HapticMotor(this)

        onCreatedView()
        onUpdate()
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
        binding.captureButton.setOnClickListener {
            takePhoto()
        }

        binding.iso.setOnClickListener {
            val sensitivityNames =
                listOf("Auto") + sensitivities.map { it.toString() }
            val defaultValue = sensitivities.indexOf(viewModel.iso.value) + 1

            val dialog = SelectableDialog(
                items = sensitivityNames,
                defaultIndexValue = defaultValue,
                title = "Modificar ISO",
                okButtonText = "OK",
                cancelButtonText = "Cancel"
            ) { selectedItem ->
                val newIso = if (selectedItem == 0) {
                    null
                } else {
                    sensitivities[selectedItem - 1]
                }
                viewModel.setIso(newIso)
                if(newIso != null) {
                    cameraStore.updateSensitivitySensor(newIso)
                }
            }
            dialog.show(supportFragmentManager, "ListSelectionDialog")
        }

        binding.shutterSpeed.setOnClickListener {
            val initial = if(viewModel.shutterSpeed.value != null) {
                viewModel.shutterSpeed.value!!.toMillis().toInt()
            } else {
                cameraStore.getCameraValues().shootSpeedMin
            }
            val dialog = SeekDialog(
                minValue = cameraStore.getCameraValues().shootSpeedMin,
                maxValue = cameraStore.getCameraValues().shootSpeedMax,
                initialValue = initial
            ) { selectedValue ->
                val newShutterSpeed = Duration.ofMillis((selectedValue.toFloat() * 1000).toLong())
                viewModel.setShutterSpeed(newShutterSpeed)
                cameraStore.updateShootSpeed(selectedValue)
            }
            dialog.show(supportFragmentManager, "IntervalPickerDialog")
        }

//        binding.interval.setOnClickListener {
//            CustomUiUtils.pickDuration(
//                applicationContext,
//                viewModel.interval.value,
//                "Interval",
//                showSeconds = true
//            ) {
//                val newInterval = it
//                if(newInterval != null) {
//                    viewModel.setInterval(newInterval)
//                }
//            }
//        }

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
        defineInitialValues()
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

    private fun onUpdate() {
        viewModel.iso.observe(this, Observer {
            val iso = it
            binding.iso.text = iso?.toString() ?: "Auto"
            binding.camera.camera?.setSensitivity(iso)
        })
        viewModel.shutterSpeed.observe(this, Observer {
            val shutterSpeed = it
            binding.shutterSpeed.text =
                shutterSpeed?.let { DecimalFormatter.format(shutterSpeed.toMillis() / 1000f, 2) }
                    ?: "Auto"
            binding.camera.camera?.setExposureTime(shutterSpeed)
            val previous = viewModel.previousShutterSpeed.value
            viewModel.setPreviousShutterSpeed(shutterSpeed)
            if (shutterSpeed != previous && previous != null && previous > Duration.ofMillis(250)) {
                restartCamera()
            }
        })
        viewModel.interval.observe(this, Observer {
            val interval = it
            binding.interval.text =
                interval?.let { DecimalFormatter.format(interval.toMillis() / 1000f, 2) }
                    ?: "Off"
            if (interval != null) {
                turnOffDuringInterval = interval > Duration.ofSeconds(2)
                intervalometer.interval(interval)
            } else {
                val wasRunning = turnOffDuringInterval
                intervalometer.stop()
                hasPendingPhoto = false
                turnOffDuringInterval = false
                if (wasRunning) {
                    restartCamera()
                }
            }
        })
    }
    private fun defineInitialValues() {
        val initialISO = cameraStore.getCameraValues().sensorSensitivity
        val initialDuration = Duration.ofMillis((cameraStore.getCameraValues().shootSpeed.toFloat() * 1000).toLong())

        viewModel.setIso(initialISO)
        viewModel.setShutterSpeed(initialDuration)
    }
}