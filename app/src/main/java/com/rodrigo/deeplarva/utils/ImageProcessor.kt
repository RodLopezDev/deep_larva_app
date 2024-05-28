package com.rodrigo.deeplarva.utils

import android.graphics.Bitmap

class ImageProcessor {
    companion object {
        fun scale (originalBitmap: Bitmap, size: Int = 128): Bitmap {
            var factor = originalBitmap.width / originalBitmap.height.toFloat()
            return Bitmap.createScaledBitmap(originalBitmap, size, (size / factor).toInt(), true)
        }
    }
}