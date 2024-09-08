package com.iiap.deeplarva.domain.view

import com.iiap.deeplarva.domain.entity.Picture

data class PictureListEntity (
    var picture: Picture,
    val state: ProcessingState? = null
) {
    companion object {
        fun none(picture: Picture): PictureListEntity {
            return PictureListEntity(picture)
        }
        fun processing(picture: Picture): PictureListEntity {
            return PictureListEntity(picture,  ProcessingState(true))
        }
        fun lockedForProcess(picture: Picture): PictureListEntity {
            return PictureListEntity(picture,  ProcessingState(false))
        }
    }
}