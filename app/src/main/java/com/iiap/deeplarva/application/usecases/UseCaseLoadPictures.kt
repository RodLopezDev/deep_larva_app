package com.iiap.deeplarva.application.usecases

import com.iiap.deeplarva.domain.view.PictureListEntity
import com.iiap.deeplarva.infraestructure.services.PicturesServices

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