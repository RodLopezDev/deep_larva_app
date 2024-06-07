package com.rodrigo.deeplarva.routes

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.media.Image
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.routes.cameraV2.CameraPro
import com.rodrigo.deeplarva.routes.cameraV2.CameraProListener
import com.rodrigo.deeplarva.routes.cameraV2.CameraUtils
import com.rodrigo.deeplarva.routes.view.CameraActivityV2View
import com.rodrigo.deeplarva.routes.view.CameraActivityViewListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class CameraV2Activity: AppCompatActivity(), CameraProListener, CameraActivityViewListener {

    private lateinit var cameraPro: CameraPro
    private lateinit var view: CameraActivityV2View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        view = CameraActivityV2View(this)
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
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")

        try {
            val output = FileOutputStream(file)
            output.write(bytes)
            output.close()
            runOnUiThread {
                Toast.makeText(this, "Image Saved: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onLogError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onError() {
        finish()
    }

    override fun onDetectCamera(cameraCharacteristics: CameraCharacteristics) {
        view.initializeCommandControl(this, CameraUtils.getCameraCharacteristic(cameraCharacteristics))
    }

    override fun onChangeExposure(exposure: Int) {
        cameraPro.updateExposure(exposure)
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
}