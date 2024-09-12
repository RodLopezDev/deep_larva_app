package com.iiap.deeplarva.gob.pe.modules.requests

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException


class RequestManager {
    companion object {
        inline fun <reified T> baseGet(url: String, headerKey: String, header: String, listener: RequestListener<T>) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader(headerKey, header)
                .url(url)
                .get()
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
                        val response = responseAdapter.fromJson(body!!)
                        if(response != null){
                            listener.onComplete(response)
                            return
                        }
                    }
                    listener.onFailure()
                }
            })
        }
        inline fun <reified T> basePost(url: String, headerKey: String, header: String, json: String, listener: RequestListener<T>) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader(headerKey, header)
                .url(url)
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
                        val response = responseAdapter.fromJson(body!!)
                        if(response != null){
                            listener.onComplete(response)
                            return
                        }
                    }
                    listener.onFailure()
                }
            })
        }
        fun putToS3(s3Url: String, file: File, listener: RequestListener<Boolean>) {
            val client = OkHttpClient()
            val requestBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
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
                        Log.d("Example", body!!)
                        listener.onComplete(true)
                        return
                    }
                    listener.onFailure()
                }
            })
        }
    }
}