package com.iiap.deeplarva.gob.pe.modules.prediction

import android.graphics.Bitmap

data class FinalResult(
    val finalBitmap: Bitmap?,
    var counter: Int,
    val boxes: List<List<Float>>,
    val totalTime: Long
)