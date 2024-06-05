package com.rodrigo.deeplarva.ui.camera

import android.graphics.ImageFormat
import android.media.ImageReader
import android.util.Range
import android.util.Size

class CameraParameters() {
    var imageDimension: Size? = null
    var exposureRange: Range<Int>? = null
    var exposureStep: Float = 0f
    var isoRange: Range<Int>? = null
    var speedRange: Range<Long>? = null

    fun update(camera: Camera) {
        imageDimension = camera.largest
        exposureRange = camera.exposureRange
        exposureStep = camera.exposureStep
        isoRange = camera.isoRange
        speedRange = camera.speedRange
    }
}