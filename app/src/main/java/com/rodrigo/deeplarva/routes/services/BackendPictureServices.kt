package com.rodrigo.deeplarva.routes.services

import android.graphics.Bitmap
import com.rodrigo.deeplarva.domain.requests.SyncPictureRequest
import com.rodrigo.deeplarva.domain.response.NewPictureResponse
import com.rodrigo.deeplarva.modules.requests.RequestListener
import com.rodrigo.deeplarva.modules.requests.RequestManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class BackendPictureServices {
    fun saveSample(payload: SyncPictureRequest, listener: RequestListener<NewPictureResponse>){
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter = moshi.adapter(SyncPictureRequest::class.java)

        val json = jsonAdapter.toJson(payload)
        RequestManager().post<NewPictureResponse>("/picture", json, listener)
    }
    fun uploadFile(response: NewPictureResponse, bitmap: Bitmap, listener: RequestListener<String>){
        RequestManager().putToS3(response.originalFileURL, bitmap, listener)
    }
    fun uploadProcessedFile(response: NewPictureResponse, bitmap: Bitmap, listener: RequestListener<String>){
        RequestManager().putToS3(response.processedFileURL, bitmap,listener)
    }
}