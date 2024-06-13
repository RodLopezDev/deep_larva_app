package com.rodrigo.deeplarva.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.application.utils.Constants

class PredictionBroadcastReceiver (private val activity: AppCompatActivity){

    private lateinit var receiver: BroadcastReceiver

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
        val filter = IntentFilter(Constants.BROADCAST_ACTION)
        activity.registerReceiver(receiver, filter)
    }

    fun unregister() {
        if(receiver != null)
            activity.unregisterReceiver(receiver)
    }
}