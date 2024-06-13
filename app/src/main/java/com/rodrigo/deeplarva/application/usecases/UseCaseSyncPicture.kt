package com.rodrigo.deeplarva.application.usecases

import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.domain.requests.SyncPictureRequest
import com.rodrigo.deeplarva.domain.response.NewPictureResponse
import com.rodrigo.deeplarva.modules.requests.RequestListener
import com.rodrigo.deeplarva.routes.services.BackendPictureServices
import com.rodrigo.deeplarva.routes.services.BoxDetectionServices
import com.rodrigo.deeplarva.routes.services.PicturesServices
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