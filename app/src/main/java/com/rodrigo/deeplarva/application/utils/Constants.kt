package com.rodrigo.deeplarva.application.utils

import android.Manifest
import android.os.Build
import com.rodrigo.deeplarva.BuildConfig
import com.rodrigo.deeplarva.utils.ColorUtils

class Constants {
    companion object {
        const val SERVICE_BASE_URL = BuildConfig.SERVER_URL
        const val SERVICE_API_KEY = BuildConfig.SERVER_API_KEY

        val OPACITY_GREEN = ColorUtils.green(90)
        val OPACITY_RED = ColorUtils.red(90)
        val GREEN_SYNC = ColorUtils.green(40)

        const val DB_NAME ="deep-larva-db"

        const val FILE_PROVIDER = "com.rodrigo.deeplarva.fileProvider"

        const val IMAGE_EXTENSION =".jpg"

        const val INTENT_PICTURE_DETAIL = "pictureId"
        const val INTENT_CAMERA_PRO_RESULT = "data"

        const val NOTIFICATION_CHANNEL_ID = "MyServiceChannel"
        const val NOTIFICATION_ID = 1

        const val BROADCAST_ACTION = "com.deeplarva.broadcast.NOTIFICATION"

        const val MESSAGE_SERVICE_STARTED = "Ejecutando conteo"
        const val MESSAGE_SERVICE_IS_RUNNING = "El conteo se está ejecutando"

        const val SHARED_PREFERENCES_DEVICE_ID = "DEVICE-IDENTIFIER"

        const val SHARED_PREFERENCES_RESOLUTION_MAX_WIDTH = "camera-resolution-max-width"
        const val SHARED_PREFERENCES_RESOLUTION_MAX_HEIGHT = "camera-resolution-max-height"
        const val SHARED_PREFERENCES_EXPOSURE_VALUE = "exposure-camera-value"
        const val SHARED_PREFERENCES_EXPOSURE_MIN = "exposure-camera-min"
        const val SHARED_PREFERENCES_EXPOSURE_MAX = "exposure-camera-max"
        const val SHARED_PREFERENCES_SENSOR_SENSITIVITY_VALUE = "sensor-sensitivity-camera-value"
        const val SHARED_PREFERENCES_SENSOR_SENSITIVITY_MIN = "sensor-sensitivity-camera-min"
        const val SHARED_PREFERENCES_SENSOR_SENSITIVITY_MAX = "sensor-sensitivity-camera-max"
        const val SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_VALUE = "sensor-exposure-time-camera-value"
        const val SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_MIN = "sensor-exposure-time-camera-min"
        const val SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_MAX = "sensor-exposure-time-camera-max"

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