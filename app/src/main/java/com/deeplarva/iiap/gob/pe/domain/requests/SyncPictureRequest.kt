package com.deeplarva.iiap.gob.pe.domain.requests

import com.deeplarva.iiap.gob.pe.domain.entity.BoxDetection
import com.deeplarva.iiap.gob.pe.domain.entity.Picture

data class SyncPictureRequest (
    val picture: Picture,
    val boxes: List<BoxDetection>
)