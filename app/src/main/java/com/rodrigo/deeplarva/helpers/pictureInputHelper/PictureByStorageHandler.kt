package com.rodrigo.deeplarva.helpers.pictureInputHelper

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.utils.BitmapUtils


class PictureByStorageHandler(override val activity: AppCompatActivity): IPictureReceiverHandler {
    companion object {
        val REQUESTCODE = 101
    }

    override fun launch(){
        if (Constants.REQUIRE_CONTRACT_FOR_GALLERY) {
            return
        }
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        activity.startActivityForResult(intent, REQUESTCODE)
    }
    override fun getBitmap(requestCode: Int, resultCode: Int, data: Intent?): List<Bitmap> {
        if (requestCode == REQUESTCODE && resultCode == Activity.RESULT_OK){
            val uri = data?.data ?: throw Exception("ERROR_GETTING_IMAGE")
            return getBitmapFromUri(uri)
        }
        throw Exception("ERROR_GETTING_IMAGE")
    }

    private fun getBitmapFromUri(uri: Uri): List<Bitmap> {
        val bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, uri)

        val filePath = getRealPathFromURI(uri)

        // Corregir la orientación del bitmap
        val correctedBitmap = BitmapUtils.correctBitmapOrientation(bitmap, filePath)

        return listOf(correctedBitmap)
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