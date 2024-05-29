package com.rodrigo.deeplarva.services

import android.app.Service
import android.content.Intent

class PredictionBroadcastSender(private val service: Service) {

    private val ACTION = "com.deeplarva.broadcast.NOTIFICATION"

    fun notify(percentage: Int) {
//        val message = "${data.isRunning}|${data.percentage}"

        val broadcastIntent = Intent(ACTION)
        broadcastIntent.putExtra("data", percentage)
        service.sendBroadcast(broadcastIntent)
    }
}