package com.rodrigo.deeplarva.domain

import com.rodrigo.deeplarva.domain.entity.SubSample
import com.rodrigo.deeplarva.utils.Colors

class MessageFactory {
    companion object {
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