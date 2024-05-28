package com.rodrigo.deeplarva.services

import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.infraestructure.driver.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PicturesServices(private val db: AppDatabase) {

    fun findOne(pictureId: Long, callback: (picture: Picture?) -> Unit){
        GlobalScope.launch {
            var pictures = db.picture().getById(pictureId)
            withContext(Dispatchers.Main) {
                if (pictures.isNotEmpty()) {
                    callback(pictures[0])
                } else {
                    callback(null)
                }
            }
        }
    }

    fun findBySubSampleId(subSampleId: Long, callback: (pictures: List<Picture>) -> Unit){
        GlobalScope.launch {
            var pictures = db.picture().getBySubSampleId(subSampleId)
            withContext(Dispatchers.Main) {
                callback(pictures)
            }
        }
    }

    fun findUnprocessedBySubSampleId(subSampleId: Long, callback: (pictures: List<Picture>) -> Unit){
        GlobalScope.launch {
            var pictures = db.picture().getBySubSampleIdNonProcessed(subSampleId)
            withContext(Dispatchers.Main) {
                callback(pictures)
            }
        }
    }

    fun findProcessedBySubSampleId(subSampleId: Long, callback: (pictures: List<Picture>) -> Unit){
        GlobalScope.launch {
            var pictures = db.picture().getBySubSampleIdProcessed(subSampleId)
            withContext(Dispatchers.Main) {
                callback(pictures)
            }
        }
    }

    fun save(subSampleId: Long, filePath: String, thumbnailPath: String, callback: () -> Unit){
        GlobalScope.launch {
            db.picture().insert(
                Picture(filePath = filePath, hasMetadata = false, count = 0, processedFilePath = "", thumbnailPath = thumbnailPath, subSampleId = subSampleId, time = 0)
            )
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }

    fun update(picture: Picture, callback: () -> Unit){
        GlobalScope.launch {
            db.picture().update(picture)
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }
}