package com.rodrigo.deeplarva.routes.services

import com.rodrigo.deeplarva.domain.Constants
import com.rodrigo.deeplarva.domain.MessageFactory
import com.rodrigo.deeplarva.domain.entity.SubSample
import com.rodrigo.deeplarva.domain.view.SubSampleItemList
import com.rodrigo.deeplarva.infraestructure.driver.AppDatabase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class SubSampleServices(private val db: AppDatabase) {
    fun findAll(callback: (subSamples: List<SubSampleItemList>) -> Unit) {
        GlobalScope.launch {
            var subSamples = db.subSample().getAllSubSamplesForUIList()
            withContext(Dispatchers.Main) {
                callback(subSamples)
            }
        }
    }

    fun findOne(subSampleId: Long, callback: (subSample: SubSample?) -> Unit){
        GlobalScope.launch {
            var subsamples = db.subSample().getById(subSampleId)
            withContext(Dispatchers.Main) {
                callback(if (subsamples.isNotEmpty()) subsamples[0] else null)
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
                    name = MessageFactory.getSubSampleName()
                )
            )
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }

    fun update(subSample: SubSample, callback: () -> Unit){
        GlobalScope.launch {
            db.subSample().update(subSample)
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }
}