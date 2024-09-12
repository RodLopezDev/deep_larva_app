package com.deeplarva.iiap.gob.pe.routes.activity.main

import com.deeplarva.iiap.gob.pe.domain.entity.Picture

interface IPictureViewListener {
    fun onRemovePicture(picture: Picture)
}