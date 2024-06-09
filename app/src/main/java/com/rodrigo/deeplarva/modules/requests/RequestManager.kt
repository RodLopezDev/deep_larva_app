package com.rodrigo.deeplarva.modules.requests

import android.graphics.Bitmap
import com.rodrigo.deeplarva.domain.Constants
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException


class RequestManager {
    fun post(url: String, json: String, listener: RequestListener) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .addHeader("api-Key", Constants.SERVICE_API_LEY)
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
                    listener.onComplete()
                } else {
                }
            }
        })
    }
    fun postWithBitmap(url: String, bitmap: Bitmap, listener: RequestListener) {
        val client = OkHttpClient()

        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val bitmapData = byteArrayOutputStream.toByteArray()

        builder.addFormDataPart(
            "image", "image.png",
            RequestBody.create("image/png".toMediaTypeOrNull(), bitmapData)
        )

        val requestBody = builder.build()
        val request = Request.Builder()
            .addHeader("api-Key", Constants.SERVICE_API_LEY)
            .url("${Constants.SERVICE_BASE_URL}$url")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onFailure()
            }
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    listener.onComplete()
                } else {
                }
            }
        })
    }
}