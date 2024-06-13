package com.rodrigo.deeplarva.application.usecases

import com.rodrigo.deeplarva.domain.view.PictureListEntity
import com.rodrigo.deeplarva.routes.services.PicturesServices

class UseCaseLoadPicturesProcessesRunning(
    private val picturesServices: PicturesServices
) {
    fun execute (pictureIdRunning: Long, callback: (list: List<PictureListEntity>) -> Unit) {
        picturesServices.findAll {
            val entityViews = it.map(PictureListEntity::none)
            val withRule = entityViews.map {it ->
                getEntity(pictureIdRunning, it)
            }
            callback(withRule)
        }
    }

    private fun getEntity(pictureIdRunning: Long, entity: PictureListEntity): PictureListEntity {
        if(entity.picture.hasMetadata) {
            return entity
        }else if (entity.picture.id == pictureIdRunning) {
            return PictureListEntity.processing(entity.picture)
        }
        return PictureListEntity.lockedForProcess(entity.picture)
    }
}