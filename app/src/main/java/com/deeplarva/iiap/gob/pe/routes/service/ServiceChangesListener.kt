package com.deeplarva.iiap.gob.pe.routes.service

interface ServiceChangesListener {
    fun onStartService(pictureId: Long)
    fun onEndService()
}