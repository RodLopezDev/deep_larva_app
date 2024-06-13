package com.rodrigo.deeplarva.services

    interface ServiceChangesListener {
    fun onStartService(pictureId: Long)
    fun onEndService()
}