package com.iiap.deeplarva.gob.pe.routes.activity.main

import com.iiap.deeplarva.gob.pe.domain.entity.Picture

interface IPictureViewListener {
    fun onRemovePicture(picture: Picture)
}