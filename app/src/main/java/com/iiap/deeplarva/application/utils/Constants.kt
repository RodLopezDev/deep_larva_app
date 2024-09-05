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

        const val CONFIG_SHARED_PREFERENCES_FLAG_INITIAL_CONFIG = "config_flag"
        const val CONFIG_SHARED_PREFERENCES_CAMERA_ACTIVITY_V2 = "config_v2"

        const val CLOUD_VALUE_LAST_DATE_CHECKED = "cloud-last-date-checked"
        const val CLOUD_VALUE_APP_VERSION = "cloud-app-version"
        const val CLOUD_VALUE_SERVER_URL = "cloud-server-url"
        const val CLOUD_VALUE_SERVER_API_KEY = "cloud-server-api-key"

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