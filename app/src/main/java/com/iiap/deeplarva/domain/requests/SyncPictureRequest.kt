package com.iiap.deeplarva.domain.requests

import com.iiap.deeplarva.domain.entity.BoxDetection
import com.iiap.deeplarva.domain.entity.Picture

data class SyncPictureRequest (
    val picture: Picture,
    val boxes: List<BoxDetection>
)