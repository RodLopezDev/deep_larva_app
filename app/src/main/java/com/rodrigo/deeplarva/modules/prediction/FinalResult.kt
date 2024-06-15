package com.rodrigo.deeplarva.modules.prediction

import android.graphics.Bitmap

data class FinalResult(
    val finalBitmap: Bitmap?,
    var counter: Int,
    val boxes: List<List<Float>>
)