package com.rodrigo.deeplarva.services

import android.app.Service
import android.content.Intent
import com.rodrigo.deeplarva.domain.Constants

class PredictionBroadcastSender(private val service: Service) {

    fun notify(percentage: Int) {
        val broadcastIntent = Intent(Constants.BROADCAST_ACTION)
        broadcastIntent.putExtra("data", percentage)
        service.sendBroadcast(broadcastIntent)
    }
}