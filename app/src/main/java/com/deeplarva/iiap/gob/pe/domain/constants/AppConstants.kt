package com.deeplarva.iiap.gob.pe.domain.constants

import com.deeplarva.iiap.gob.pe.BuildConfig

class AppConstants {
    companion object {
        const val SERVICE_BASE_URL = BuildConfig.APP_CONFIG_URL
        const val SERVICE_API_KEY = BuildConfig.APP_CONFIG_API_KEY

        const val DB_NAME ="deep-larva-db"

        const val FILE_PROVIDER = "com.deeplarva.iiap.gob.pe.fileProvider"

        const val INTENT_PICTURE_DETAIL = "pictureId"
        const val INTENT_CAMERA_PRO_RESULT = "data"

        const val NOTIFICATION_CHANNEL_ID = "MyServiceChannel"
        const val NOTIFICATION_ID = 1

        const val BROADCAST_ACTION = "com.deeplarva.broadcast.NOTIFICATION"

        const val IMAGE_EXTENSION =".jpg"

        const val FOLDER_PICTURES = "deep-larva"
    }
}