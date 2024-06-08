package com.rodrigo.deeplarva.utils.image

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.rodrigo.deeplarva.domain.Constants
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoByCameraHandler(override val activity: Activity): IPhotoHandler {

    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    private val REQUESTCODE = 102
    private lateinit var currentPhotoPath: String

    override fun launch(){
        val permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            return
        }

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // TODO:
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            // Manejar el error al crear el archivo
            return
        }

        // Continuar solo si el archivo se creó correctamente
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(activity, Constants.FILE_PROVIDER, it)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            activity.startActivityForResult(takePictureIntent, REQUESTCODE)
        }
    }
    override fun getBitmap(requestCode: Int, resultCode: Int, data: Intent?): List<Bitmap> {
        if (requestCode == REQUESTCODE && resultCode == Activity.RESULT_OK){
            val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
            return listOf(bitmap)
        }
        throw Exception("ERROR_GETTING_IMAGE")
    }
    override fun getRequestCode(): Int{
        return REQUESTCODE
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir: File? = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, Constants.IMAGE_EXTENSION, storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    fun onRequestComplete(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    launch()
                } else {
                     Toast.makeText(activity, "Camera permission denied.", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }

    }
}