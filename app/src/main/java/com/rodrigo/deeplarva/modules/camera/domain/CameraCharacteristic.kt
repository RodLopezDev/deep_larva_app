package com.rodrigo.deeplarva.modules.camera.domain

import android.util.Range
import android.util.Size

data class CameraCharacteristic (
    val largest: Size,
    val isoRange: Range<Int>,
    val speedRange: Range<Long>
)