package com.odrigo.recognitionappkt.routes.pictures.facades

interface ViewFacade {
    fun eventPredict()
    fun enableDeletion(index: Int)
    fun addDeletable(index: Int)
}