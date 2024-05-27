package com.odrigo.recognitionappkt.drivers.photo

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
    override fun getBitmap(requestCode: Int, resultCode: Int, data: Intent?): Bitmap {
        if (requestCode == REQUESTCODE && resultCode == Activity.RESULT_OK){
             return MediaStore.Images.Media.getBitmap(activity.contentResolver, data?.data)
        }
        throw Exception("ERROR_GETTING_IMAGE")
    }
    override fun getRequestCode(): Int{
        return REQUESTCODE
    }
}