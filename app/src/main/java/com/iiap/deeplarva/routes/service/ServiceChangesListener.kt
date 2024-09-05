package com.iiap.deeplarva.routes.service

interface ServiceChangesListener {
    fun onStartService(pictureId: Long)
    fun onEndService()
}