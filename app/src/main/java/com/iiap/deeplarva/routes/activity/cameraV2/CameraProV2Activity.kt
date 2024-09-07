package com.iiap.deeplarva.routes.activity.cameraV2

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.iiap.deeplarva.databinding.ActivityCameraPro2Binding
import com.iiap.deeplarva.domain.constants.AppConstants
import com.iiap.deeplarva.domain.constants.SharedPreferencesConstants
import com.iiap.deeplarva.modules.camerapro2.infraestructure.SensitivityProvider
import com.iiap.deeplarva.modules.CameraParameterStore
import com.iiap.deeplarva.ui.widget.dialogs.SeekDialog
import com.iiap.deeplarva.ui.widget.dialogs.SelectableDialog
import com.iiap.deeplarva.ui.widget.dialogs.ShooterSpeedDialog
import com.iiap.deeplarva.utils.PreferencesHelper
import com.iiap.deeplarva.utils.SpeedUtils
import com.kylecorry.andromeda.camera.ImageCaptureSettings
import com.kylecorry.andromeda.core.math.DecimalFormatter
import com.kylecorry.andromeda.core.time.CoroutineTimer
import com.kylecorry.andromeda.files.LocalFileSystem
import com.kylecorry.andromeda.haptics.HapticFeedbackType
import com.kylecorry.andromeda.haptics.HapticMotor
import com.kylecorry.luna.coroutines.CoroutineQueueRunner
import com.kylecorry.luna.coroutines.onIO
import com.kylecorry.luna.coroutines.onMain
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

    private var hasFirstSetting = false
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
        deviceID = PreferencesHelper(this).getString(SharedPreferencesConstants.DEVICE_ID) ?: ""
        viewModel = ViewModelProvider(this)[CameraV2Model::class.java]

        binding = ActivityCameraPro2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        files = LocalFileSystem(this)
        haptics = HapticMotor(this)

        onCreatedView()
        onUpdate()
    }

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

            defineInitialValues()
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

        binding.containerISO.setOnClickListener {
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

        binding.containerShutter.setOnClickListener {
            //val min = cameraStore.getCameraValues().shootSpeedMin / 1000000
            //val max = cameraStore.getCameraValues().shootSpeedMax / 1000000
            val dialog = ShooterSpeedDialog(
                title = "Shooter Speed"
            ) {
                val selectedValue = SpeedUtils.adjustSpeed(it)
                val inNanoseconds =  selectedValue * 1000000L

                viewModel.setShutterSpeed(selectedValue)
                cameraStore.updateShootSpeed(inNanoseconds)
            }
            dialog.show(supportFragmentManager, "IntervalPickerDialog")
        }

        binding.containerInterval.setOnClickListener {
            val initial = if(viewModel.interval.value != null) {
                viewModel.interval.value!!.toSeconds().toInt()
            } else {
                0
            }
            val dialog = SeekDialog(
                minValue = 0,
                maxValue = 10,
                initialValue = initial,
                title = "Modificar Interval"
            ) { selectedValue ->
                if (selectedValue == null) {
                    viewModel.setInterval(null)
                    cameraStore.updateExposure(0)
                    return@SeekDialog
                }
                val newInterval = Duration.ofNanos((selectedValue.toFloat() * 1000).toLong())
                viewModel.setInterval(newInterval)
                cameraStore.updateExposure(selectedValue)
            }
            dialog.show(supportFragmentManager, "IntervalPickerDialog")
        }
        binding.containerExposure.setOnClickListener {
            val initial = viewModel.exposure.value ?: 0
            val dialog = SeekDialog(
                minValue = cameraStore.getCameraValues().exposureMin,
                maxValue = cameraStore.getCameraValues().exposureMax,
                initialValue = initial,
                title = "Modificar exposicióm"
            ) { selectedValue ->
                viewModel.setExposure(selectedValue)
                cameraStore.updateExposure(selectedValue)
            }
            dialog.show(supportFragmentManager, "ExposurePickerDialog")
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
                val file = File(imageFolder, "$fileName${AppConstants.IMAGE_EXTENSION}")

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
            returnIntent.putExtra(AppConstants.INTENT_CAMERA_PRO_RESULT, intentData)
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
            val shutterSpeed = it ?: 100
            binding.shutterSpeed.text = SpeedUtils.speedToText(shutterSpeed)

            val duration = Duration.ofMillis(shutterSpeed.toLong())
            binding.camera.camera?.setExposureTime(duration)


            val previous = viewModel.previousShutterSpeed.value
            viewModel.setPreviousShutterSpeed(shutterSpeed)
            if (shutterSpeed != previous && previous != null && previous > 250) {
                restartCamera()
            }
        })
        viewModel.exposure.observe(this, Observer {
            val exposure = it
            if(exposure != null) {
                binding.exposure.text = exposure.toString()
                binding.camera.camera?.setExposure(exposure)
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
        if(hasFirstSetting) {
            return
        }

        val initialISO = cameraStore.getCameraValues().sensorSensitivity
        val initialExposure = cameraStore.getCameraValues().exposure
//        val initialDuration = Duration.ofNanos((cameraStore.getCameraValues().shootSpeed.toFloat()).toLong())
        val initialDuration = cameraStore.getCameraValues().shootSpeed / 1000000

        viewModel.setIso(initialISO)
        viewModel.setExposure(initialExposure)
        viewModel.setShutterSpeed(initialDuration.toInt())

        hasFirstSetting = true
    }
}