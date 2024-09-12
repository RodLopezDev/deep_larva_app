package com.iiap.deeplarva.routes.activity.main

import com.iiap.deeplarva.domain.entity.Picture

interface IPictureViewListener {
    fun onRemovePicture(picture: Picture)
}