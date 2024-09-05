package com.iiap.deeplarva.modules.camera

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraPermissionsManager(
    private val activity: AppCompatActivity,
    private val listener: ICameraPermissionsResult
) {
    companion object{
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
    fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                listener.onGranted()
            } else {
                Toast.makeText(activity, "Debes proporcionar permisos si quieres tomar fotos", Toast.LENGTH_LONG).show()
                activity.finish()
            }
        }
    }
    fun openWithRequest() {
        if (allPermissionsGranted()) listener.onGranted()
        else ActivityCompat.requestPermissions(activity,
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_PERMISSIONS
        )
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
    }
}