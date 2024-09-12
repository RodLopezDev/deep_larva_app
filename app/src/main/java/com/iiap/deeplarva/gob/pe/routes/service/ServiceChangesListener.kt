package com.iiap.deeplarva.gob.pe.routes.service

interface ServiceChangesListener {
    fun onStartService(pictureId: Long)
    fun onEndService()
}