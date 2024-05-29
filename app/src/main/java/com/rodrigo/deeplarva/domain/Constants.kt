package com.rodrigo.deeplarva.domain

import com.rodrigo.deeplarva.domain.entity.SubSample
import com.rodrigo.deeplarva.utils.Colors

class Constants {
    companion object {
        val OPACITY_GREEN = Colors.green(90)
        val OPACITY_RED = Colors.red(90)

        const val DB_NAME ="deep-larva-db"

        const val FILE_PROVIDER = "com.rodrigo.deeplarva.fileProvider"

        const val IMAGE_EXTENSION =".png"


        const val NOTIFICATION_CHANNEL_ID = "MyServiceChannel"
        const val NOTIFICATION_ID = 1

        const val BROADCAST_ACTION = "com.deeplarva.broadcast.NOTIFICATION"

        fun getSubSampleName(): String {
            return "Pruebas"
        }

        fun getResultsView(subSample: SubSample): String {
            return "Moda : ${subSample.mean} individuos\nMax : ${subSample.min} individuos\nMin : ${subSample.max} individuos"
        }

        fun getPictureResults(hasResult: Boolean, count: Int): String {
            return "Conteo: ${count}\nTiempo: 00:00:00\nPred: ${if(hasResult){"SI"}else{"NO"}}"
        }
    }
}