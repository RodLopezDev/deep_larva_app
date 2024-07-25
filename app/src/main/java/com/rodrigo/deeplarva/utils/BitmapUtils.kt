package com.rodrigo.deeplarva.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import com.rodrigo.deeplarva.application.utils.Constants
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.UUID

class BitmapUtils {
    companion object {
        fun getRandomBitmapName(): String {
            var uuid: UUID = UUID.randomUUID()
            var uuidString: String = uuid.toString()
            return "${uuidString}${Constants.IMAGE_EXTENSION}"
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
        fun correctBitmapOrientation(bitmap: Bitmap, filePath: String): Bitmap {
            val exif = try {
                ExifInterface(filePath)
            } catch (e: IOException) {
                e.printStackTrace()
                return bitmap
            }

            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        fun fixBitmapOrientation(filePath: String, defaultBitmap: Bitmap): Bitmap {
            val exif = try {
                ExifInterface(filePath)
            } catch (e: IOException) {
                e.printStackTrace()
                return defaultBitmap
            }
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            return Bitmap.createBitmap(defaultBitmap, 0, 0, defaultBitmap.width, defaultBitmap.height, matrix, true)
        }
        fun imageToBitmap(image: android.media.Image): Bitmap? {
            val buffer: ByteBuffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees)
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
        fun getBitmapFromUri(context: Context, uri: Uri): List<Bitmap> {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

            val filePath = getRealPathFromURI(context, uri)

            // Corregir la orientación del bitmap
            val correctedBitmap = correctBitmapOrientation(bitmap, filePath)

            return listOf(correctedBitmap)
        }
        private fun getRealPathFromURI(context: Context, uri: Uri): String {
            var filePath = ""
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                if (idx != -1) {
                    filePath = cursor.getString(idx)
                }
                cursor.close()
            }
            return filePath
        }
    }
}