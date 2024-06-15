package com.rodrigo.deeplarva.routes.services

import com.rodrigo.deeplarva.domain.entity.BoxDetection
import com.rodrigo.deeplarva.infraestructure.internal.driver.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BoxDetectionServices(private val db: AppDatabase) {
    fun saveBulk(pictureId: Long, boxes: List<List<Float>>, callback: () -> Unit){
        GlobalScope.launch {
            boxes.forEach {
                db.boxDetection().insert(
                    BoxDetection(0, pictureId, it[0].toInt(), it[1].toInt(), it[2].toInt(), it[3].toInt())
                )
            }
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }

    fun findByPictureId(pictureId: Long, callback: (boxes: List<BoxDetection>) -> Unit){
        GlobalScope.launch {
            var boxes = db.boxDetection().getByPictureId(pictureId)
            withContext(Dispatchers.Main) {
                callback(boxes.ifEmpty { mutableListOf<BoxDetection>() })
            }
        }
    }
}