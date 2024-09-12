package com.iiap.deeplarva.ui.adapter

import com.iiap.deeplarva.domain.entity.Picture

interface PictureItemListListener {
    fun onPredict(picture: Picture)
}