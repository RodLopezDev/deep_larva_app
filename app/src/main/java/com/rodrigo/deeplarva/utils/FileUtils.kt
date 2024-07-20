package com.rodrigo.deeplarva.utils

import android.graphics.Bitmap
import android.media.Image
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.application.utils.Constants
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class FileUtils(private val activity: AppCompatActivity) {
    fun saveOnStorage(image: Image, path: String, fileName: String): File {
        val imageFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), path)
        if (!imageFolder.exists()) {
            imageFolder.mkdirs()
        }

        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        val file = File(imageFolder, "$fileName${Constants.IMAGE_EXTENSION}")
        try {
            val output = FileOutputStream(file)
            output.write(bytes)
            output.close()
            return file
        } catch (e: IOException) {
            throw e
        }
    }
    fun saveBitmapToExternalStorage(bitmap: Bitmap, path: String, filename: String): File {
        val imageFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), path)
        if (!imageFolder.exists()) {
            imageFolder.mkdirs()
        }

        val file = File(imageFolder, "$filename${Constants.IMAGE_EXTENSION}")

        try {
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream?.flush()
            fileOutputStream?.close()
            return file
        } catch (e: IOException) {
            throw e
        }
    }
}