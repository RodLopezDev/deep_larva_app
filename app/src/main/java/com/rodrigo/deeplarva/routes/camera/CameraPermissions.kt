package com.rodrigo.deeplarva.routes.camera

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.rodrigo.deeplarva.routes.camera.interfaces.CameraPermissionsListener

class CameraPermissions(private val activity: AppCompatActivity, private val listener: CameraPermissionsListener) {

    private val REQUEST_CAMERA_PERMISSION = 1

    fun check(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d("CameraPermissions", permissions.toString())
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                listener.onInitCamera()
                return
            }
            listener.onRejectCamera()
        }
    }

    fun request() {
        if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            return
        }
        listener.onInitCamera()
    }
}