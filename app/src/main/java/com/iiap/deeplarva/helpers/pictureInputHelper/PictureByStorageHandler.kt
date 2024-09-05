package com.iiap.deeplarva.helpers.pictureInputHelper

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.iiap.deeplarva.domain.constants.PermissionsConstans
import com.iiap.deeplarva.utils.BitmapUtils


class PictureByStorageHandler(override val activity: AppCompatActivity): IPictureReceiverHandler {
    companion object {
        val REQUESTCODE = 101
    }

    override fun launch(){
        if (PermissionsConstans.REQUIRE_CONTRACT_FOR_GALLERY) {
            return
        }
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        activity.startActivityForResult(intent, REQUESTCODE)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getBitmap(requestCode: Int, resultCode: Int, data: Intent?): List<Bitmap> {
        if (requestCode == REQUESTCODE && resultCode == Activity.RESULT_OK){
            val bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, data?.data)

            val BitmapList: MutableList<Bitmap> = mutableListOf()

            // Corregir la orientación del bitmap
            val rotatedBitmap = BitmapUtils.correctBitmapOrientation(bitmap, BitmapUtils.getRealPathFromURI(activity, data?.data ?: throw Exception("ERROR_GETTING_IMAGE")))

            // Auto-recortado de bitmap segun region de interes detectado
            val croppedBitmap = BitmapUtils.autoCropImage(activity, rotatedBitmap)

            croppedBitmap.apply {
                if (croppedBitmap != null) {
                    BitmapList.add(croppedBitmap)
                }
            }

            return BitmapList
//            return listOf(rotatedBitmap)
        }
        throw Exception("ERROR_GETTING_IMAGE")
    }
    override fun getRequestCode(): Int{
        return REQUESTCODE
    }
}