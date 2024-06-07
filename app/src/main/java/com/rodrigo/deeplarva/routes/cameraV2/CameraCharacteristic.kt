package com.rodrigo.deeplarva.routes.cameraV2

import android.util.Range
import android.util.Size

data class CameraCharacteristic (
    val largest: Size,
    val exposureRange: Range<Int>,
    val exposureStep: Float,
    val isoRange: Range<Int>,
    val speedRange: Range<Long>
)