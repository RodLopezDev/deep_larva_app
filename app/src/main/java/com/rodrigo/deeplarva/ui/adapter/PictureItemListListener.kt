package com.rodrigo.deeplarva.ui.adapter

import com.rodrigo.deeplarva.domain.entity.Picture

interface PictureItemListListener {
    fun onPredict(picture: Picture)
}