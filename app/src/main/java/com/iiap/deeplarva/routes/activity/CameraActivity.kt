package com.iiap.deeplarva.routes.activity

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
import android.os.Bundle
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.iiap.deeplarva.domain.constants.AppConstants
import com.iiap.deeplarva.domain.constants.SharedPreferencesConstants
import com.iiap.deeplarva.helpers.PreferencesHelper
import com.iiap.deeplarva.modules.camera.CameraPermissionsManager
import com.iiap.deeplarva.modules.camera.CameraProHardware
import com.iiap.deeplarva.modules.camera.CameraProHardwareListener
import com.iiap.deeplarva.modules.camera.ICameraPermissionsResult
import com.iiap.deeplarva.routes.activity.stores.CameraParameterStore
import com.iiap.deeplarva.routes.activity.view.CameraActivityView
import com.iiap.deeplarva.routes.activity.view.ICameraViewListener
import com.iiap.deeplarva.utils.BitmapUtils
import com.iiap.deeplarva.utils.FileUtils
import java.io.IOException

class CameraActivity: AppCompatActivity() {

    private val pictures = mutableListOf<String>()
    private lateinit var cameraProHW: CameraProHardware
    private lateinit var view: CameraActivityView
    private lateinit var permissions: CameraPermissionsManager

    private var deviceID: String = ""
    private lateinit var cameraStore: CameraParameterStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraStore = CameraParameterStore(this)
        deviceID = PreferencesHelper(this).getString(SharedPreferencesConstants.DEVICE_ID) ?: ""
    }

    override fun onResume() {
        super.onResume()
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation = windowManager.defaultDisplay.rotation

        view = CameraActivityView(this, cameraStore.getCameraValues(), object: ICameraViewListener {
            override fun onTakePicture() {
                cameraProHW.takePicture()
            }
            override fun onClose() {
                onCloseView()
            }
            override fun onUpdateExposure(value: Int) {
                cameraStore.updateExposure(value)
                cameraProHW.updateExposure(value)
            }
            override fun onUpdateSensitivitySensor(value: Int) {
                cameraStore.updateSensitivitySensor(value)
                cameraProHW.updateISO(value)
            }
            override fun onUpdateShootSpeed(value: Int) {
                cameraStore.updateShootSpeed(value.toLong())
                cameraProHW.updateSpeed(value.toLong())
            }
        })
        cameraProHW = CameraProHardware(this, view.getPreview(), cameraStore.getCameraValues(), sensorOrientation, rotation, object: CameraProHardwareListener {
            override fun onReceivePicture(image: Image, sensorOrientationOut: Int, windowRotationOut: Int) {
                val bitmap = BitmapUtils.imageToBitmap(image)
                if(bitmap == null) {
                    Toast.makeText(this@CameraActivity, "Error getting image", Toast.LENGTH_SHORT).show()
                    onCloseView()
                    return
                }
                val rotateDegrees = when (windowRotationOut) {
                    Surface.ROTATION_0 -> 90
                    Surface.ROTATION_90 -> 0
                    Surface.ROTATION_270 -> 180
                    else -> 0
                }
                val bitmapRotated = BitmapUtils.rotateBitmap(bitmap, rotateDegrees.toFloat())
                val fileName = "${deviceID}-${System.currentTimeMillis()}"

                try {
                    val file = FileUtils(this@CameraActivity).saveBitmapToExternalStorage(bitmapRotated, "/deep-larva/", fileName)
                    val filePath = file.absolutePath

                    pictures.add(filePath)
                    runOnUiThread {
                        Toast.makeText(this@CameraActivity, "Image Added: $filePath", Toast.LENGTH_SHORT).show()
                    }
                    onCloseView()
                } catch (e: IOException) {
//                    listener.onError("CameraActivity.onReceivePicture.SaveOnStorage::${e?.message}")
                }
            }
            override fun onError(message: String, critical: Boolean) {
            }
            override fun onCameraLoaded() {
            }
        })
        permissions = CameraPermissionsManager(this, object: ICameraPermissionsResult {
            override fun onGranted() {
                cameraProHW.onStart()
            }
        })
        permissions.openWithRequest()
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
}