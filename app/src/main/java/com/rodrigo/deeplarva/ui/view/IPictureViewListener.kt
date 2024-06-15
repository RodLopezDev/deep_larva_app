package com.rodrigo.deeplarva.ui.view

import com.rodrigo.deeplarva.domain.entity.Picture

interface IPictureViewListener {
    fun onRemovePicture(picture: Picture)
}