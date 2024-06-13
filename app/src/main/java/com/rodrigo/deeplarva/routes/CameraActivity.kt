package com.rodrigo.deeplarva.routes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.media.Image
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.routes.camera.CameraPro
import com.rodrigo.deeplarva.routes.camera.interfaces.CameraProListener
import com.rodrigo.deeplarva.routes.camera.utils.CameraUtils
import com.rodrigo.deeplarva.routes.view.CameraActivityView
import com.rodrigo.deeplarva.routes.camera.interfaces.CameraActivityViewListener
import com.rodrigo.deeplarva.utils.Files
import java.io.IOException

class CameraActivity: AppCompatActivity(), CameraProListener, CameraActivityViewListener {

    private var photos = mutableListOf<String>()
    private lateinit var cameraPro: CameraPro
    private lateinit var view: CameraActivityView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        view = CameraActivityView(this)
        cameraPro = CameraPro(this, view.textureView, this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraPro.init()
            return
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        }else {
            cameraPro.init()
        }
    }

    override fun onReceivePicture(image: Image) {
        val fileName = "${System.currentTimeMillis()}-RUNNING-IDENTIFIER"
        try {
            val file = Files(this).SaveOnStorage(image, "/deep-larva/", fileName)
            val filePath = file.absolutePath

            photos.add(filePath)
            runOnUiThread {
                Toast.makeText(this, "Image Added: $filePath", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            onLogError("CameraActivity.onReceivePicture.SaveOnStorage::${e?.message}")
        }
    }

    override fun onLogError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onError() {
        finish()
    }

    override fun onCameraLoaded() {
        view.showTextureView()
    }

    override fun onDetectCamera(cameraCharacteristics: CameraCharacteristics) {
        view.initializeCommandControl(this, CameraUtils.getCameraCharacteristic(cameraCharacteristics))
    }

    override fun onChangeISO(exposure: Int) {
        cameraPro.updateISO(exposure)
    }

    override fun onChangeSpeed(exposure: Long) {
        cameraPro.updateSpeed(exposure)
    }

    override fun onCapture() {
        cameraPro.takePicture()
    }

    override fun onCloseView() {
        val returnIntent = Intent()
        if(photos.isEmpty()) {
            setResult(RESULT_CANCELED, returnIntent)
        } else {
            val intentData = photos.joinToString(",,,")
            returnIntent.putExtra(Constants.INTENT_CAMERA_PRO_RESULT, intentData)
            setResult(RESULT_OK, returnIntent)
        }
        finish()
    }
}