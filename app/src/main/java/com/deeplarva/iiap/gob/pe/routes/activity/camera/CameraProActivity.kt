package com.deeplarva.iiap.gob.pe.routes.activity.camera

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.deeplarva.iiap.gob.pe.R
import com.deeplarva.iiap.gob.pe.application.adapters.CameraParameterAdapter
import com.deeplarva.iiap.gob.pe.databinding.ActivityCameraProBinding
import com.deeplarva.iiap.gob.pe.domain.constants.AppConstants
import com.deeplarva.iiap.gob.pe.domain.constants.SharedPreferencesConstants
import com.deeplarva.iiap.gob.pe.modules.camerapro.infraestructure.SensitivityProvider
import com.deeplarva.iiap.gob.pe.ui.widget.dialogs.ExposureDialog
import com.deeplarva.iiap.gob.pe.ui.widget.dialogs.ISODialog
import com.deeplarva.iiap.gob.pe.ui.widget.dialogs.ShutterSpeedDialog
import com.deeplarva.iiap.gob.pe.utils.CameraUtils
import com.deeplarva.iiap.gob.pe.utils.ExposureUtils
import com.deeplarva.iiap.gob.pe.utils.PreferencesHelper
import com.kylecorry.andromeda.camera.ImageCaptureSettings
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

class CameraProActivity: AppCompatActivity() {
    private var deviceID: String = ""
    private val pictures = mutableListOf<String>()
    private lateinit var binding: ActivityCameraProBinding
    private lateinit var files: LocalFileSystem
    private lateinit var haptics: HapticMotor

    private lateinit var preferencesHelper: PreferencesHelper

    private lateinit var viewModel: CameraModel
    private lateinit var cameraStore: CameraParameterAdapter

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
        preferencesHelper = PreferencesHelper(this)
        val characteristics = CameraUtils.getMainCameraCharacteristics(this)

        cameraStore = CameraParameterAdapter(preferencesHelper, characteristics)
        deviceID = PreferencesHelper(this).getString(SharedPreferencesConstants.DEVICE_ID) ?: ""
        viewModel = ViewModelProvider(this)[CameraModel::class.java]

        binding = ActivityCameraProBinding.inflate(layoutInflater)
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
            val initialValue = viewModel.iso.value
            val dialog = ISODialog(
                title = getString(R.string.dialog_title_iso),
                preferencesHelper = preferencesHelper,
                initialValue = initialValue ?: 0
            ) {
                cameraStore.updateSensitivitySensor(it)
                viewModel.setIso(it)
            }
            dialog.show(supportFragmentManager, "IsoPickerDialog")
        }

        binding.containerShutterSpeed.setOnClickListener {
            val preferencesHelper = PreferencesHelper(this)
            val initialValue = viewModel.shutterSpeed.value!!
            val dialog = ShutterSpeedDialog(
                activity = this,
                preferencesHelper = preferencesHelper,
                initialValue = initialValue,
                title = "Shooter Speed"
            ) { value, text ->
                cameraStore.updateShootSpeed(value, text)
                viewModel.setShutterSpeed(value)
            }
            dialog.show(supportFragmentManager, "ShutterSpeedPickerDialog")
        }

        binding.containerExposure.setOnClickListener {
            val stepValue = preferencesHelper.getFloat(SharedPreferencesConstants.EXPOSURE_VALID_STEP, 0F)

            val initial = viewModel.exposure.value ?: 0
            val dialog = ExposureDialog(
                minValue = CameraParameterAdapter.EXPOSURE_MIN,
                maxValue = CameraParameterAdapter.EXPOSURE_MAX,
                initialValue = initial,
                step = stepValue,
                title = getString(R.string.dialog_title_exposure),
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
            if(iso == 0) {
                binding.iso.text = "Auto"
                binding.camera.camera?.setSensitivity(null)
                return@Observer
            }
            binding.iso.text = iso?.toString() ?: "Auto"
            binding.camera.camera?.setSensitivity(iso)
        })
        viewModel.shutterSpeed.observe(this, Observer {
            val shutterSpeed = it ?: 100
            val shutterText = preferencesHelper.getString(SharedPreferencesConstants.SHUTTER_SPEED_TIME_TEXT) ?: ""
            binding.shutterSpeed.text = shutterText

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
                val stepValue = preferencesHelper.getFloat(SharedPreferencesConstants.EXPOSURE_VALID_STEP, 0F)
                binding.exposure.text = ExposureUtils.convertLocalToLabel(exposure, stepValue)
                binding.camera.camera?.setExposure(exposure)
            }
        })
    }
    private fun defineInitialValues() {
        if(hasFirstSetting) {
            return
        }

        val initialISO = cameraStore.getCameraValues().sensorSensitivity
        val initialExposure = cameraStore.getCameraValues().exposure
        val initialShutterSpeed = cameraStore.getCameraValues().shootSpeed

        viewModel.setIso(initialISO)
        viewModel.setExposure(initialExposure)
        viewModel.setShutterSpeed(initialShutterSpeed)

        hasFirstSetting = true
    }
}