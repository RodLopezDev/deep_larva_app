package com.rodrigo.deeplarva.domain

import com.rodrigo.deeplarva.utils.Colors

class Constants {
    companion object {
        val OPACITY_GREEN = Colors.green(90)
        val OPACITY_RED = Colors.red(90)

        const val DB_NAME ="deep-larva-db"

        const val FILE_PROVIDER = "com.rodrigo.deeplarva.fileProvider"

        const val IMAGE_EXTENSION =".jpeg"

        const val INTENT_CAMERA_PRO_RESULT = "data"

        const val NOTIFICATION_CHANNEL_ID = "MyServiceChannel"
        const val NOTIFICATION_ID = 1

        const val BROADCAST_ACTION = "com.deeplarva.broadcast.NOTIFICATION"

        const val MESSAGE_SERVICE_STARTED = "Ejecutando conteo"
        const val MESSAGE_SERVICE_DISCONNECTED = "Servicio sin comunicación"
        const val MESSAGE_SERVICE_RUNNING = "Procesamiento en ejecución"
        const val MESSAGE_ERROR_LOADING_IMAGE = "Error al cargar imagen"
    }
}