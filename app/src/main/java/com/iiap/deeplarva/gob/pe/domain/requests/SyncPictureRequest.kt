package com.iiap.deeplarva.gob.pe.domain.requests

import com.iiap.deeplarva.gob.pe.domain.entity.BoxDetection
import com.iiap.deeplarva.gob.pe.domain.entity.Picture

data class SyncPictureRequest (
    val picture: Picture,
    val boxes: List<BoxDetection>
)