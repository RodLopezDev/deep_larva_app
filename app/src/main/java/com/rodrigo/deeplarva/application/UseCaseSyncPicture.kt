package com.rodrigo.deeplarva.application

import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.domain.requests.SyncPictureRequest
import com.rodrigo.deeplarva.modules.requests.RequestListener
import com.rodrigo.deeplarva.modules.requests.RequestManager
import com.rodrigo.deeplarva.routes.services.BoxDetectionServices
import com.rodrigo.deeplarva.routes.services.PicturesServices
import com.rodrigo.deeplarva.utils.BitmapUtils
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class UseCaseSyncPicture(private val picturesServices: PicturesServices, private val boxDetectionServices: BoxDetectionServices) {
    fun run (picture: Picture, listener: RequestListener){
        val originalBitmap = BitmapUtils.getBitmapFromPath(picture.filePath)
        val processedBitmap = BitmapUtils.getBitmapFromPath(picture.processedFilePath)
        if(originalBitmap == null || processedBitmap == null) {
            listener.onFailure()
            return
        }

        boxDetectionServices.findByPictureId(picture.id) {boxes -> run {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val jsonAdapter = moshi.adapter(SyncPictureRequest::class.java)

            val objPayload = SyncPictureRequest(picture, boxes)
            val json = jsonAdapter.toJson(objPayload)
            RequestManager().post("/picture", json, object: RequestListener {
                override fun onFailure() {
                    listener.onFailure()
                }
                override fun onComplete() {
                    RequestManager().postWithBitmap("/bitmap/file/${picture.uuid}", originalBitmap, object: RequestListener {
                        override fun onFailure() {
                            listener.onFailure()
                        }
                        override fun onComplete() {
                            RequestManager().postWithBitmap("/bitmap/processed/${picture.uuid}", processedBitmap, object: RequestListener {
                                override fun onFailure() {
                                    listener.onFailure()
                                }
                                override fun onComplete() {
                                    picturesServices.update(picture.copy(syncWithCloud = true)) {
                                        listener.onComplete()
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