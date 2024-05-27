package com.odrigo.recognitionappkt.drivers.photo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoByCameraHandler(override val activity: Activity): IPhotoHandler {
    private val REQUESTCODE = 102
    private lateinit var currentPhotoPath: String

    override fun launch(){
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
            val photoURI: Uri = FileProvider.getUriForFile(activity, "com.odrigo.recognitionappkt.fileprovider", it)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            activity.startActivityForResult(takePictureIntent, REQUESTCODE)
        }
    }
    override fun getBitmap(requestCode: Int, resultCode: Int, data: Intent?): Bitmap {
        if (requestCode == REQUESTCODE && resultCode == Activity.RESULT_OK){
            return BitmapFactory.decodeFile(currentPhotoPath)
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
        return File.createTempFile(imageFileName, ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }
}