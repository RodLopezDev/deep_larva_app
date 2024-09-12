package com.iiap.deeplarva.gob.pe.routes.service.broadcast

import android.app.Service
import android.content.Intent
import com.iiap.deeplarva.gob.pe.domain.constants.AppConstants

class PredictionBroadcastSender(private val service: Service) {

    fun notify(pictureId: Long, percentage: Int) {
        val mockEncryptedMessage = "$pictureId|$percentage"

        val broadcastIntent = Intent(AppConstants.BROADCAST_ACTION)
        broadcastIntent.putExtra("data", mockEncryptedMessage)
        service.sendBroadcast(broadcastIntent)
    }
}