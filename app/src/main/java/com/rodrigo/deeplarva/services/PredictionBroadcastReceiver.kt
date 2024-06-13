package com.rodrigo.deeplarva.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.application.utils.Constants

class PredictionBroadcastReceiver (private val activity: AppCompatActivity){

    private lateinit var receiver: BroadcastReceiver

    fun register(callback: (percentage: Int) -> Unit) {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val percentage = intent?.getIntExtra("data", 0) ?: return
                callback(percentage)
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