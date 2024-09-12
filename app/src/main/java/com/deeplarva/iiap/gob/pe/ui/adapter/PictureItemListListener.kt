package com.deeplarva.iiap.gob.pe.ui.adapter

import com.deeplarva.iiap.gob.pe.domain.entity.Picture

interface PictureItemListListener {
    fun onPredict(picture: Picture)
}