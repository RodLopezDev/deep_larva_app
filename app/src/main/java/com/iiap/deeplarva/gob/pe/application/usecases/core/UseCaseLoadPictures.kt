package com.iiap.deeplarva.gob.pe.application.usecases.core

import com.iiap.deeplarva.gob.pe.domain.view.PictureListEntity
import com.iiap.deeplarva.gob.pe.infraestructure.services.PicturesServices

class UseCaseLoadPictures(
    private val picturesServices: PicturesServices
) {
    fun execute (callback: (list: List<PictureListEntity>) -> Unit) {
        picturesServices.findAll {
            val entityViews = it.map(PictureListEntity::none)
            callback(entityViews)
        }
    }
}