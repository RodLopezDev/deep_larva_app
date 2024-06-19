package com.rodrigo.deeplarva.routes.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import com.google.android.material.snackbar.Snackbar
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.modules.camerav2.CameraPermissionsManager
import com.rodrigo.deeplarva.modules.camerav2.CameraV2Pro
import com.rodrigo.deeplarva.modules.camerav2.ICameraPermissionsResult
import com.rodrigo.deeplarva.modules.camerav2.ICameraV2ProListener
import com.rodrigo.deeplarva.routes.activity.view.CameraV2ActivityView
import com.rodrigo.deeplarva.routes.activity.view.ICameraV2ViewListener
import java.util.UUID

class CameraV2Activity: AppCompatActivity() {

    private val pictures = mutableListOf<String>()
    private lateinit var cameraPro: CameraV2Pro
    private lateinit var view: CameraV2ActivityView
    private lateinit var permissions: CameraPermissionsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = CameraV2ActivityView(this, object: ICameraV2ViewListener {
            override fun onTakePicture() {
                cameraPro.takePicture()
            }
            override fun onUpdateExposure(value: Int) {
                cameraPro.updateISO(value)
            }
            override fun onClose() {
                onCloseView()
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
        cameraPro = CameraV2Pro(this, object: ICameraV2ProListener {
            override fun getFolderName(): String {
                return "deep-larva"
            }
            override fun getPictureFileName(): String {
                return UUID.randomUUID().toString()
            }
            override fun getPreviewView(): PreviewView {
                return view.getPreview()
            }
            override fun onPictureReceived(internalUri: String, contentUri: String) {
                val cl = view.getLinearLayout()
                Snackbar.make(cl, "Imagen guardada con éxito", Snackbar.LENGTH_LONG).setAction("OK") {
                    cl.setBackgroundColor(Color.CYAN)
                }.show()

                pictures.add(internalUri)
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