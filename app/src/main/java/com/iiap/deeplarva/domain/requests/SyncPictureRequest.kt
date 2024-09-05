package com.rodrigo.deeplarva.domain.requests

import com.rodrigo.deeplarva.domain.entity.BoxDetection
import com.rodrigo.deeplarva.domain.entity.Picture

data class SyncPictureRequest (
    val picture: Picture,
    val boxes: List<BoxDetection>
)