package com.rodrigo.deeplarva.routes.service.broadcast

import android.app.Service
import android.content.Intent
import com.rodrigo.deeplarva.application.utils.Constants

class PredictionBroadcastSender(private val service: Service) {

    fun notify(pictureId: Long, percentage: Int) {
        val mockEncryptedMessage = "$pictureId|$percentage"

        val broadcastIntent = Intent(Constants.BROADCAST_ACTION)
        broadcastIntent.putExtra("data", mockEncryptedMessage)
        service.sendBroadcast(broadcastIntent)
    }
}