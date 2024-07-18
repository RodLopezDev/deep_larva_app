package com.rodrigo.deeplarva.routes.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import com.google.android.material.snackbar.Snackbar
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.helpers.PreferencesHelper
import com.rodrigo.deeplarva.modules.camera.CameraPermissionsManager
import com.rodrigo.deeplarva.modules.camera.CameraPro
import com.rodrigo.deeplarva.modules.camera.ICameraPermissionsResult
import com.rodrigo.deeplarva.modules.camera.ICameraProListener
import com.rodrigo.deeplarva.routes.activity.view.CameraActivityView
import com.rodrigo.deeplarva.routes.activity.view.ICameraViewListener
import java.util.UUID

class CameraActivity: AppCompatActivity() {

    private val pictures = mutableListOf<String>()
    private lateinit var cameraPro: CameraPro
    private lateinit var view: CameraActivityView
    private lateinit var permissions: CameraPermissionsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferencesHelper = PreferencesHelper(this)
        view = CameraActivityView(this, object: ICameraViewListener {
            override fun onTakePicture() {
                cameraPro.takePicture()
            }
            override fun onClose() {
                onCloseView()
            }
            override fun onUpdateExposure(value: Int) {
                preferencesHelper.saveInt(Constants.SHARED_PREFERENCES_EXPOSURE_VALUE, value)
                cameraPro.updateExposure(value)
            }
            override fun getMinExposure(): Int {
                return -20
            }
            override fun getMaxExposure(): Int {
                return 20
            }
            override fun getDefaultExposure(): Int {
                val exposure = preferencesHelper.getInt(Constants.SHARED_PREFERENCES_EXPOSURE_VALUE, 0)
                return exposure
            }
        })
        cameraPro = CameraPro(this, object: ICameraProListener {
            override fun getFolderName(): String {
                return Constants.FOLDER_PICTURES
            }
            override fun getPictureFileName(): String {
                return UUID.randomUUID().toString()
            }
            override fun getPreviewView(): PreviewView {
                return view.getPreview()
            }
            override fun onPictureReceived(picturePath: String) {
                val cl = view.getLinearLayout()
                Snackbar.make(cl, "Imagen guardada con éxito", Snackbar.LENGTH_LONG).setAction("OK") {
                    cl.setBackgroundColor(Color.CYAN)
                }.show()

                pictures.add(picturePath)
                onCloseView()
            }
            override fun onErrorPicture() {
                val cl = view.getLinearLayout()
                Snackbar.make(cl, "Error al guardar la imagen", Snackbar.LENGTH_LONG).setAction("OK") {
                    cl.setBackgroundColor(Color.CYAN)
                }.show()
                onCloseView()
            }
        })
        permissions = CameraPermissionsManager(this, object: ICameraPermissionsResult {
            override fun onGranted() {
                cameraPro.startCamera()
            }
        })
    }
    override fun onResume() {
        super.onResume()
        permissions.openWithRequest()
    }
    override fun onRequestPermissionsResult(requestCode: Int, ps: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, ps, grantResults)
        permissions.onRequestPermissionsResult(requestCode, ps, grantResults)
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraPro.offCamera()
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