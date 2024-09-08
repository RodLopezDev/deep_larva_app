package com.iiap.deeplarva.routes.service.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.iiap.deeplarva.domain.constants.AppConstants

class PredictionBroadcastReceiver (private val activity: AppCompatActivity){

    private lateinit var receiver: BroadcastReceiver

    @RequiresApi(Build.VERSION_CODES.O)
    fun register(callback: (pictureId: Long, percentage: Int) -> Unit) {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val mockEncryptedMessage = intent?.getStringExtra("data") ?: return
                val splitValues = mockEncryptedMessage.split("|")
                val pictureId = splitValues[0].toLong()
                val percentage = splitValues[1].toInt()
                callback(pictureId, percentage)
            }
        }
        val filter = IntentFilter(AppConstants.BROADCAST_ACTION)
        activity.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
    }

    fun unregister() {
        if(receiver != null)
            activity.unregisterReceiver(receiver)
    }
}