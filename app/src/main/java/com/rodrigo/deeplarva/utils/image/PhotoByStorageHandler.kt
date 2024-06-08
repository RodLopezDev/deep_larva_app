package com.rodrigo.deeplarva.utils.image

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore

class PhotoByStorageHandler(override val activity: Activity): IPhotoHandler {
    private val REQUESTCODE = 101
    override fun launch(){
        val intent = Intent()
        intent.setAction(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        activity.startActivityForResult(intent, REQUESTCODE)
    }
    override fun getBitmap(requestCode: Int, resultCode: Int, data: Intent?): List<Bitmap> {
        if (requestCode == REQUESTCODE && resultCode == Activity.RESULT_OK){
            val bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, data?.data)
            return listOf(bitmap)
        }
        throw Exception("ERROR_GETTING_IMAGE")
    }
    override fun getRequestCode(): Int{
        return REQUESTCODE
    }
}