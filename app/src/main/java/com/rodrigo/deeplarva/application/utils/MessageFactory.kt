package com.rodrigo.deeplarva.application.utils

import com.rodrigo.deeplarva.utils.TimeUtils

class MessageFactory {
    companion object {
        fun getSubSampleName(): String {
            return "Pruebas"
        }

        fun getResultsView(): String {
            return ""
        }

        fun getPictureResults(hasResult: Boolean, count: Int, time: Long): String {
            return "Conteo: ${count}\nTiempo: ${TimeUtils.formatDuration(time)}\nPred: ${if(hasResult){"SI"}else{"NO"}}"
        }
    }
}