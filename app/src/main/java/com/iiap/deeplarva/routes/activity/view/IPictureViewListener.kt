package com.iiap.deeplarva.routes.activity.view

import com.iiap.deeplarva.domain.entity.Picture

interface IPictureViewListener {
    fun onRemovePicture(picture: Picture)
}