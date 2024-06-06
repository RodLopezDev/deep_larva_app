package com.rodrigo.deeplarva.domain

import com.rodrigo.deeplarva.domain.entity.SubSample
import com.rodrigo.deeplarva.utils.Colors
import com.rodrigo.deeplarva.utils.Time

class MessageFactory {
    companion object {
        fun getSubSampleName(): String {
            return "Pruebas"
        }

        fun getResultsView(subSample: SubSample): String {
            return "Moda : ${subSample.mean} individuos\nMax : ${subSample.min} individuos\nMin : ${subSample.max} individuos"
        }

        fun getPictureResults(hasResult: Boolean, count: Int, time: Long): String {
            return "Conteo: ${count}\nTiempo: ${Time.formatDuration(time)}\nPred: ${if(hasResult){"SI"}else{"NO"}}"
        }
    }
}