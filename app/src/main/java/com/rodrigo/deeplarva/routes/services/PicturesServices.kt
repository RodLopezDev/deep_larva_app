package com.rodrigo.deeplarva.routes.services

import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.domain.utils.BitmapProcessingResult
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

    fun findAll(callback: (pictures: List<Picture>) -> Unit){
        GlobalScope.launch {
            var pictures = db.picture().getAllPictures()
            withContext(Dispatchers.Main) {
                callback(pictures)
            }
        }
    }

    fun findUnprocessed(callback: (pictures: List<Picture>) -> Unit){
        GlobalScope.launch {
            var pictures = db.picture().getAllNonProcessed()
            withContext(Dispatchers.Main) {
                callback(pictures)
            }
        }
    }

    fun findProcessed(callback: (pictures: List<Picture>) -> Unit){
        GlobalScope.launch {
            var pictures = db.picture().getAllProcessed()
            withContext(Dispatchers.Main) {
                callback(pictures)
            }
        }
    }

    fun save(filePath: String, thumbnailPath: String, timeStamp: Long, callback: () -> Unit){
        GlobalScope.launch {
            db.picture().insert(
                Picture(filePath = filePath, hasMetadata = false, count = 0, processedFilePath = "", thumbnailPath = thumbnailPath, time = 0, timestamp = timeStamp)
            )
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }

    fun saveBulk(items: List<BitmapProcessingResult>, callback: () -> Unit){
        GlobalScope.launch {
            items.map {
                db.picture().insert(
                    Picture(filePath = it.filePath, hasMetadata = false, count = 0, processedFilePath = "", thumbnailPath = it.thumbnailPath, time = 0, timestamp = it.timestamp)
                )
            }
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