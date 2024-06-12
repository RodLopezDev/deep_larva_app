package com.rodrigo.deeplarva.modules.requests

import android.graphics.Bitmap
import com.rodrigo.deeplarva.domain.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


class RequestManager {
    inline fun <reified T> post(url: String, json: String, listener: RequestListener<T>) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .addHeader("x-api-key", Constants.SERVICE_API_KEY)
            .url("${Constants.SERVICE_BASE_URL}$url")
            .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onFailure()
            }
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                    val responseAdapter = moshi.adapter(T::class.java)
                    val response = responseAdapter.fromJson(body)
                    if(response != null){
                        listener.onComplete(response)
                        return
                    }
                }
                listener.onFailure()
            }
        })
    }
    fun putToS3(s3Url: String, file: File, listener: RequestListener<String>) {
        val client = OkHttpClient()
        val requestBody = RequestBody.create("image/png".toMediaTypeOrNull(), file)
        val request = Request.Builder()
            .url(s3Url)
            .put(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onFailure()
            }
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    listener.onComplete("")
                    return
                }
                listener.onFailure()
            }
        })
    }
}