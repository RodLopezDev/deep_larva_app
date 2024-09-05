package com.iiap.deeplarva.application.utils

import android.Manifest
import android.os.Build
import com.iiap.deeplarva.BuildConfig
import com.iiap.deeplarva.utils.ColorUtils

class Constants {
    companion object {
        const val SERVICE_BASE_URL = BuildConfig.APP_CONFIG_URL
        const val SERVICE_API_KEY = BuildConfig.APP_CONFIG_API_KEY

        val GREEN_SYNC = ColorUtils.green(40)

        val REQUIRE_CONTRACT_FOR_GALLERY: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

        const val MAX_ISO = 3200
        const val MIN_ISO = 100
        const val MAX_SHOOT_SPEED = 10000000L
        const val MIN_SHOOT_SPEED = 100000L

        const val MILI_TO_NANO_SECONDS = 1000000L

        const val DB_NAME ="deep-larva-db"

        const val FILE_PROVIDER = "com.iiap.deeplarva.fileProvider"

        const val IMAGE_EXTENSION =".jpg"

        const val INTENT_PICTURE_DETAIL = "pictureId"
        const val INTENT_CAMERA_PRO_RESULT = "data"

        const val NOTIFICATION_CHANNEL_ID = "MyServiceChannel"
        const val NOTIFICATION_ID = 1

        const val BROADCAST_ACTION = "com.deeplarva.broadcast.NOTIFICATION"

        const val MESSAGE_SERVICE_STARTED = "Ejecutando conteo"
        const val MESSAGE_SERVICE_IS_RUNNING = "El conteo se está ejecutando"

        const val FOLDER_PICTURES = "deep-larva"

        fun getPermissionsList (): List<String> {
            // REF: https://stackoverflow.com/questions/75628155/read-external-storage-permission-request-not-showing-on-emulator
            // REF: https://stackoverflow.com/questions/72948052/android-13-read-external-storage-permission-still-usable
            val sdk = Build.VERSION.SDK_INT

            val permissions = mutableListOf<String>(Manifest.permission.INTERNET, Manifest.permission.CAMERA)
            if(sdk <= Build.VERSION_CODES.S){
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            return permissions
        }
    }
}