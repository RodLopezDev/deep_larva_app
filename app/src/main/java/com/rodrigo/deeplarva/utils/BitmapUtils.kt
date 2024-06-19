package com.rodrigo.deeplarva.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.rodrigo.deeplarva.application.utils.Constants
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class BitmapUtils {
    companion object {
        fun getRandomBitmapName(): String {
            var uuid: UUID = UUID.randomUUID()
            var uuidString: String = uuid.toString()
            return "$uuidString.${Constants.IMAGE_EXTENSION}"
        }
        fun getBitmapFromPath(filePath: String): Bitmap? {
            return BitmapFactory.decodeFile(filePath)
        }
        fun saveBitmapToStorage(context: Context, bitmap: Bitmap, filename: String): String? {
            val file = File(context.getExternalFilesDir(null), filename)
            try {
                // Create a file output stream
                val fos = FileOutputStream(file)

                // Compress the bitmap to a PNG with 100% quality
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)

                // Close the file output stream
                fos.close()

                // Return the absolute path of the saved file
                return file.absolutePath
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
        fun scale (originalBitmap: Bitmap, size: Int = 128): Bitmap {
            var factor = originalBitmap.width / originalBitmap.height.toFloat()
            return Bitmap.createScaledBitmap(originalBitmap, size, (size / factor).toInt(), true)
        }
    }
}