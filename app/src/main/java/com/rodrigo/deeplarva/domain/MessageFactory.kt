package com.rodrigo.deeplarva.domain

import com.rodrigo.deeplarva.utils.Time

class MessageFactory {
    companion object {
        fun getSubSampleName(): String {
            return "Pruebas"
        }

        fun getResultsView(): String {
            return ""
        }

        fun getPictureResults(hasResult: Boolean, count: Int, time: Long): String {
            return "Conteo: ${count}\nTiempo: ${Time.formatDuration(time)}\nPred: ${if(hasResult){"SI"}else{"NO"}}"
        }
    }
}