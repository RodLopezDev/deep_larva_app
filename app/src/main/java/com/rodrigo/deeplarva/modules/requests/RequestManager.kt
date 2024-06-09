package com.rodrigo.deeplarva.modules.requests

import android.graphics.Bitmap
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException


class RequestManager {
    private val baseUrl = "https://webhook.site"
    private val apiKey = "123123123"
    fun post(url: String, json: String, listener: RequestListener) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .addHeader("api-Key", apiKey)
            .url("$baseUrl$url")
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
//
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val bitmapData = byteArrayOutputStream.toByteArray()

        // Add image part
        builder.addFormDataPart(
            "image", "image.png",
            RequestBody.create("image/png".toMediaTypeOrNull(), bitmapData)
        )

        val requestBody = builder.build()
        val request = Request.Builder()
            .addHeader("api-Key", apiKey)
            .url("$baseUrl$url")
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

    companion object {
        fun sendBitmapWithData(url: String, bitmap: Bitmap, data: Map<String, String>) {
            // Convert Bitmap to ByteArray
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val bitmapData = byteArrayOutputStream.toByteArray()

            // Create MultipartBody
            val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)

            // Add data parts
            for ((key, value) in data) {
                builder.addFormDataPart(key, value)
            }

            // Add image part
            builder.addFormDataPart(
                "image", "image.png",
                RequestBody.create("image/png".toMediaTypeOrNull(), bitmapData)
            )

            // Build request
            val requestBody = builder.build()
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            // Send request
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    // Handle the error
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        println("Upload successful: ${response.body?.string()}")
                    } else {
                        println("Upload failed: ${response.message}")
                    }
                }
            })
        }
    }
}