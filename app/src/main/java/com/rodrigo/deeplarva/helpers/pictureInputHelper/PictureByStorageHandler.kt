package com.rodrigo.deeplarva.helpers.pictureInputHelper

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.rodrigo.deeplarva.utils.BitmapUtils

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
            val correctedBitmap = BitmapUtils.correctBitmapOrientation(bitmap, filePath)

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

    override fun getRequestCode(): Int{
        return REQUESTCODE
    }
}