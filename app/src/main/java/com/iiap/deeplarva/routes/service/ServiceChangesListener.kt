package com.rodrigo.deeplarva.routes.service

interface ServiceChangesListener {
    fun onStartService(pictureId: Long)
    fun onEndService()
}