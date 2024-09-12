package com.iiap.deeplarva.gob.pe.application.usecases.cloud

import com.iiap.deeplarva.gob.pe.domain.entity.Picture
import com.iiap.deeplarva.gob.pe.domain.requests.SyncPictureRequest
import com.iiap.deeplarva.gob.pe.domain.response.NewPictureResponse
import com.iiap.deeplarva.gob.pe.infraestructure.services.BackendPictureServices
import com.iiap.deeplarva.gob.pe.infraestructure.services.BoxDetectionServices
import com.iiap.deeplarva.gob.pe.infraestructure.services.PicturesServices
import com.iiap.deeplarva.gob.pe.modules.requests.RequestListener
import java.io.File

class UseCaseSyncPicture(
    private val picturesServices: PicturesServices,
    private val boxDetectionServices: BoxDetectionServices,
    private val backendPictureServices: BackendPictureServices,
) {
    fun run (picture: Picture, listener: RequestListener<String>){
        val originalBitmap = File(picture.filePath)
        val processedBitmap = File(picture.processedFilePath)
        if(originalBitmap == null || processedBitmap == null) {
            listener.onFailure()
            return
        }
        boxDetectionServices.findByPictureId(picture.id) {boxes -> run {
            val objPayload = SyncPictureRequest(picture, boxes)
            backendPictureServices.saveSample(objPayload, object: RequestListener<NewPictureResponse> {
                override fun onFailure() {
                    listener.onFailure()
                }
                override fun onComplete(response: NewPictureResponse) {
                    if(response.circuitBreak == true){
                        picturesServices.update(picture.copy(syncWithCloud = true)) {
                            listener.onComplete("")
                        }
                        return
                    }
                    backendPictureServices.uploadFile(response, originalBitmap, object: RequestListener<Boolean> {
                        override fun onFailure() {
                            listener.onFailure()
                        }
                        override fun onComplete(r1: Boolean) {
                            backendPictureServices.uploadProcessedFile(response, processedBitmap, object: RequestListener<Boolean> {
                                override fun onFailure() {
                                    listener.onFailure()
                                }
                                override fun onComplete(r2: Boolean) {
                                    picturesServices.update(picture.copy(syncWithCloud = true)) {
                                        listener.onComplete("")
                                    }
                                }
                            })
                        }
                    })
                }
            })
        }}
    }
}