package com.rodrigo.deeplarva.utils

import android.media.Image
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class Files(private val activity: AppCompatActivity) {
    fun SaveOnStorage(image: Image, path: String, fileName: String): File {
        val imageFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), path)
        if (!imageFolder.exists()) {
            imageFolder.mkdirs()
        }

        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        val file = File(imageFolder, "$fileName.jpg")
        try {
            val output = FileOutputStream(file)
            output.write(bytes)
            output.close()
            return file
        } catch (e: IOException) {
            throw e
        }
    }
}