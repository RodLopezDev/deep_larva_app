package com.iiap.deeplarva.gob.pe.ui.adapter

import com.iiap.deeplarva.gob.pe.domain.entity.Picture

interface PictureItemListListener {
    fun onPredict(picture: Picture)
}