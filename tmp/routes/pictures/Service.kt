package com.odrigo.recognitionappkt.routes.pictures

import androidx.appcompat.app.AppCompatActivity
import com.odrigo.recognitionappkt.db.AppDatabase
import com.odrigo.recognitionappkt.db.BDFactory
import com.odrigo.recognitionappkt.domain.Picture
import com.odrigo.recognitionappkt.domain.SubSample
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Service(activity: AppCompatActivity) {
    private var db: AppDatabase

    init {

        db = BDFactory.getInstance(activity)
    }

    fun getPictureCOROUTINE(pictureId: Long, callback: (picture: Picture?) -> Unit){
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

    fun getPicturesCOROUTINE(subSampleId: Long, callback: (pictures: List<Picture>) -> Unit){
        GlobalScope.launch {
            var pictures = db.picture().getBySubSampleId(subSampleId)
            withContext(Dispatchers.Main) {
                callback(pictures)
            }
        }
    }

    fun getUnProcessedPicturesCOROUTINE(subSampleId: Long, callback: (pictures: List<Picture>) -> Unit){
        GlobalScope.launch {
            var pictures = db.picture().getBySubSampleIdNonProcessed(subSampleId)
            withContext(Dispatchers.Main) {
                callback(pictures)
            }
        }
    }

    fun getProcessedPicturesCOROUTINE(subSampleId: Long, callback: (pictures: List<Picture>) -> Unit){
        GlobalScope.launch {
            var pictures = db.picture().getBySubSampleIdProcessed(subSampleId)
            withContext(Dispatchers.Main) {
                callback(pictures)
            }
        }
    }

    fun createPictureCOROUTINE(filePath: String, subSampleId: Long, callback: () -> Unit){
        GlobalScope.launch {
            db.picture().insert(
                Picture(filePath = filePath, hasMetadata = false, count = 0, processedFilePath = "", subSampleId = subSampleId)
            )
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }

    fun updatePictureCOROUTINE(picture: Picture, callback: () -> Unit){
        GlobalScope.launch {
            db.picture().update(picture)
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }

    fun getSubSampleCOROUTINE(subSampleId: Long, callback: (subSample: SubSample?) -> Unit){
        GlobalScope.launch {
            var subsamples = db.subSample().getById(subSampleId)
            withContext(Dispatchers.Main) {
                if (subsamples.isNotEmpty()) {
                    callback(subsamples[0])
                } else {
                    callback(null)
                }
            }
        }
    }

    fun updateSubSampleCOROUTINE(subSample: SubSample, callback: () -> Unit){
        GlobalScope.launch {
            db.subSample().update(subSample)
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }
}