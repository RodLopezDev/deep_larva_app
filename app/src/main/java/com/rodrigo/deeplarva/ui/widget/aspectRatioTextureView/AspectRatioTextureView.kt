package com.rodrigo.deeplarva.ui.widget.aspectRatioTextureView

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView

class AspectRatioTextureView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr) {

    private var aspectRatioWidth: Int = 9
    private var aspectRatioHeight: Int = 16

    fun setAspectRatio(width: Int, height: Int) {
        aspectRatioWidth = width
        aspectRatioHeight = height
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)

        if (width > height * aspectRatioWidth / aspectRatioHeight) {
            width = height * aspectRatioWidth / aspectRatioHeight
        } else {
            height = width * aspectRatioHeight / aspectRatioWidth
        }

        setMeasuredDimension(width, height)
    }
}