package com.rodrigo.deeplarva.routes.services

import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.domain.view.BitmapProcessingResult
import com.rodrigo.deeplarva.infraestructure.internal.driver.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

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

    fun findProcessedNonSync(callback: (pictures: List<Picture>) -> Unit){
        GlobalScope.launch {
            var pictures = db.picture().getAllProcessedNonSync()
            withContext(Dispatchers.Main) {
                callback(pictures)
            }
        }
    }

    fun save(deviceId: String, filePath: String, thumbnailPath: String, timeStamp: Long, callback: () -> Unit){
        GlobalScope.launch {
            db.picture().insert(
                Picture(filePath = filePath, deviceId = deviceId, uuid = UUID.randomUUID().toString(), hasMetadata = false, count = 0, processedFilePath = "", thumbnailPath = thumbnailPath, time = 0, timestamp = timeStamp, syncWithCloud = false)
            )
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }

    fun saveBulk(deviceId: String, items: List<BitmapProcessingResult>, callback: () -> Unit){
        GlobalScope.launch {
            items.map {
                db.picture().insert(
                    Picture(filePath = it.filePath, deviceId = deviceId, uuid = UUID.randomUUID().toString(), hasMetadata = false, count = 0, processedFilePath = "", thumbnailPath = it.thumbnailPath, time = 0, timestamp = it.timestamp, syncWithCloud = false)
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

    fun remove(picture: Picture, callback: () -> Unit){
        GlobalScope.launch {
            db.picture().delete(picture)
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }
}