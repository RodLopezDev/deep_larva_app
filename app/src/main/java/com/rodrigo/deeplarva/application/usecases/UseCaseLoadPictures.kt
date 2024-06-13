package com.rodrigo.deeplarva.application.usecases

import com.rodrigo.deeplarva.domain.view.PictureListEntity
import com.rodrigo.deeplarva.routes.services.PicturesServices

class UseCaseLoadPictures(
    private val picturesServices: PicturesServices
) {
    fun run (callback: (list: List<PictureListEntity>) -> Unit) {
        picturesServices.findAll {
            val entityViews = it.map(PictureListEntity::none)
            callback(entityViews)
        }
    }
}