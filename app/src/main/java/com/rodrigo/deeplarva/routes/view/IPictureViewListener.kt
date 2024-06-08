package com.rodrigo.deeplarva.routes.view

import com.rodrigo.deeplarva.domain.entity.Picture

interface IPictureViewListener {
    fun onRemovePicture(picture: Picture)
}