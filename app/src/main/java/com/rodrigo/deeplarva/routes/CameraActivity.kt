package com.rodrigo.deeplarva.routes

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraDevice
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.routes.camera.Camera
import com.rodrigo.deeplarva.routes.camera.CameraPermissions
import com.rodrigo.deeplarva.routes.camera.interfaces.CameraPermissionsListener
import com.rodrigo.deeplarva.routes.camera.interfaces.CameraRenderListener
import com.rodrigo.deeplarva.routes.camera.CameraTextureView
import com.rodrigo.deeplarva.routes.camera.CameraThreadUpdater
import com.rodrigo.deeplarva.routes.camera.RenderizerCamera
import com.rodrigo.deeplarva.routes.camera.interfaces.CameraActionListener
import com.rodrigo.deeplarva.routes.camera.interfaces.CameraOnTouchListener
import com.rodrigo.deeplarva.routes.observables.CameraParamsViewModel
import com.rodrigo.deeplarva.routes.view.CameraActivityView
import java.nio.ByteBuffer

class CameraActivity: AppCompatActivity(), CameraPermissionsListener, CameraRenderListener, CameraOnTouchListener, CameraActionListener {

    private lateinit var viewModel: CameraParamsViewModel
    private lateinit var view: CameraActivityView
    private lateinit var cameraTextureView: CameraTextureView

    private val cameraThreadUpdater = CameraThreadUpdater()
    private val renderizerCamera = RenderizerCamera(this)
    private val cameraPermissions = CameraPermissions(this, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        viewModel = ViewModelProvider(this)[CameraParamsViewModel::class.java]
        view = CameraActivityView(this, viewModel)

        viewModel.ev.observe(this) {
            view.setEvText(it!!)
            renderizerCamera.updateRender(it, viewModel.iso.value!!, viewModel.speed.value!!, cameraThreadUpdater.getHandler())
        }
        viewModel.iso.observe(this) {
            view.setISOText(it!!)
            renderizerCamera.updateRender(viewModel.ev.value!!, it, viewModel.speed.value!!, cameraThreadUpdater.getHandler())
        }
        viewModel.speed.observe(this) {
            view.setSpeedText(it!!)
            renderizerCamera.updateRender(viewModel.ev.value!!, viewModel.iso.value!!, it, cameraThreadUpdater.getHandler())
        }

        cameraTextureView = CameraTextureView(this, this)

        cameraPermissions.request()

        view.getBtnCapture().setOnClickListener {
            renderizerCamera.takePicture(
                this@CameraActivity,
                cameraTextureView.getImageReader(),
                cameraThreadUpdater.getHandler()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        cameraThreadUpdater.onStart()
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            cameraPermissions.request()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraPermissions.check(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    override fun onInitCamera() {
        val camera = Camera(this, this)
        cameraTextureView.render(camera, cameraThreadUpdater.getHandler())
    }

    override fun onRejectCamera() {
        Toast.makeText(this, "Unauthorizaed camera", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onOpened(camera: Camera, cameraDevice: CameraDevice) {
        renderizerCamera.render(camera, cameraDevice, cameraTextureView.getTextureView(), cameraThreadUpdater.getHandler(), cameraTextureView.getImageReader())
    }

    override fun onDisconnected(camera: CameraDevice) {
        renderizerCamera.delete(camera)
    }

    override fun onError(camera: CameraDevice, error: Int) {
        TODO("Not yet implemented")
    }

    override fun onTouch(x: Float, y: Float) {
        renderizerCamera.onTouch(x, y, cameraTextureView.getTextureView(), cameraThreadUpdater.getHandler())
    }

    override fun getFileName(): String {
        TODO("Not yet implemented")
    }

    override fun onReceivePicture(image: Image) {
        Toast.makeText(this, "Photo taken", Toast.LENGTH_SHORT).show()
    }

    override fun onFailReceivePicture() {
        Toast.makeText(this, "Error taken photo", Toast.LENGTH_SHORT).show()
    }
}