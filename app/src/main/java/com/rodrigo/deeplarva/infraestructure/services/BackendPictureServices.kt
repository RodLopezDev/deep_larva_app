package com.rodrigo.deeplarva.infraestructure.services

import com.rodrigo.deeplarva.domain.requests.SyncPictureRequest
import com.rodrigo.deeplarva.domain.response.NewPictureResponse
import com.rodrigo.deeplarva.modules.requests.RequestListener
import com.rodrigo.deeplarva.modules.requests.RequestManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

class BackendPictureServices(
    private val serverUrl: String,
    private val serverApiKey: String
) {
    fun saveSample(payload: SyncPictureRequest, listener: RequestListener<NewPictureResponse>){
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter = moshi.adapter(SyncPictureRequest::class.java)

        val json = jsonAdapter.toJson(payload)
        val url = "${serverUrl}/picture";
        RequestManager.basePost(url, "x-api-key", serverApiKey, json, listener)
    }
    fun uploadFile(response: NewPictureResponse, file: File, listener: RequestListener<Boolean>){
        RequestManager.putToS3(response.originalFileURL, file, listener)
    }
    fun uploadProcessedFile(response: NewPictureResponse, file: File, listener: RequestListener<Boolean>){
        RequestManager.putToS3(response.processedFileURL, file,listener)
    }
}