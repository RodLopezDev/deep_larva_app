package com.rodrigo.deeplarva.helpers.pictureInputHelper

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import java.io.IOException

class PictureByStorageHandler(override val activity: Activity): IPictureReceiverHandler {
    private val REQUESTCODE = 101
    override fun launch(){
        val intent = Intent()
        intent.setAction(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        activity.startActivityForResult(intent, REQUESTCODE)
    }
    override fun getBitmap(requestCode: Int, resultCode: Int, data: Intent?): List<Bitmap> {
        if (requestCode == REQUESTCODE && resultCode == Activity.RESULT_OK){
            val uri = data?.data ?: throw Exception("ERROR_GETTING_IMAGE")
            val bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, uri)

            val filePath = getRealPathFromURI(uri)

            // Corregir la orientación del bitmap
            val correctedBitmap = correctBitmapOrientation(bitmap, filePath)

            return listOf(correctedBitmap)
        }
        throw Exception("ERROR_GETTING_IMAGE")
    }

    private fun getRealPathFromURI(uri: Uri): String {
        var filePath = ""
        val cursor = activity.contentResolver.query(uri, null, null, null, null)
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

    private fun correctBitmapOrientation(bitmap: Bitmap, filePath: String): Bitmap {
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

    override fun getRequestCode(): Int{
        return REQUESTCODE
    }
}