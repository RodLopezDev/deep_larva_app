package com.rodrigo.deeplarva.ml

import android.graphics.Bitmap

data class FinalResult(
    val finalBitmap: Bitmap?,
    var counter: Int,
    val boxes: List<List<Float>>
)