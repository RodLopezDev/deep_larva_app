package com.rodrigo.deeplarva.services

import com.rodrigo.deeplarva.domain.entity.SubSample
import com.rodrigo.deeplarva.domain.view.SubSampleItemList
import com.rodrigo.deeplarva.infraestructure.driver.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubSampleServices(private val db: AppDatabase) {
    fun load(callback: (subSamples: List<SubSampleItemList>) -> Unit) {
        GlobalScope.launch {
            var subSamples = db.subSample().getAllSubSamplesForUIList()
            withContext(Dispatchers.Main) {
                callback(subSamples)
            }
        }
    }

    fun save(callback: () -> Unit) {
        GlobalScope.launch {
            db.subSample().insert(
                SubSample(
                    isTraining = false,
                    max = 0f,
                    mean = 0f,
                    min = 0f,
                    average = 0f,
                    name = "Pruebas"
                )
            )
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }
}