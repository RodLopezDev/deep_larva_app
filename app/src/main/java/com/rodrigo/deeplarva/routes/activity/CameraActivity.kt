package com.rodrigo.deeplarva.routes.activity

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.modules.camera.CameraPermissionsManager
import com.rodrigo.deeplarva.modules.camera.CameraProHardware
import com.rodrigo.deeplarva.modules.camera.CameraProHardwareListener
import com.rodrigo.deeplarva.modules.camera.ICameraPermissionsResult
import com.rodrigo.deeplarva.routes.activity.stores.CameraParameterStore
import com.rodrigo.deeplarva.routes.activity.view.CameraActivityView
import com.rodrigo.deeplarva.routes.activity.view.ICameraViewListener
import com.rodrigo.deeplarva.utils.FileUtils
import java.io.IOException

class CameraActivity: AppCompatActivity() {

    private val pictures = mutableListOf<String>()
    private lateinit var cameraProHW: CameraProHardware
    private lateinit var view: CameraActivityView
    private lateinit var permissions: CameraPermissionsManager

    private lateinit var cameraStore: CameraParameterStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraStore = CameraParameterStore(this)
    }

    override fun onResume() {
        super.onResume()
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
                cameraStore.updateShootSpeed(value)
                cameraProHW.updateSpeed(value.toLong())
            }
        })
        cameraProHW = CameraProHardware(this, view.getPreview(), cameraStore.getCameraValues(), object: CameraProHardwareListener {
            override fun onReceivePicture(image: Image) {
                val fileName = "${System.currentTimeMillis()}-RUNNING-IDENTIFIER"
                try {
                    val file = FileUtils(this@CameraActivity).saveOnStorage(image, "/deep-larva/", fileName)
                    val filePath = file.absolutePath

                    pictures.add(filePath)
                    runOnUiThread {
                        Toast.makeText(this@CameraActivity, "Image Added: $filePath", Toast.LENGTH_SHORT).show()
                    }
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
            returnIntent.putExtra(Constants.INTENT_CAMERA_PRO_RESULT, intentData)
            setResult(RESULT_OK, returnIntent)
        }
        finish()
    }
}