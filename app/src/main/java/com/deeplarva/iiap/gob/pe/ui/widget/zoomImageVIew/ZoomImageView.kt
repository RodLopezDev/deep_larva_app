package com.deeplarva.iiap.gob.pe.ui.widget.zoomImageVIew

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector

class ZoomImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyle) {

    private val backgroundPaint: Paint = Paint().apply {
        setARGB(255, 0, 0, 0)
        style = Paint.Style.FILL
    }

    private var mPosX = 0f
    private var mPosY = 0f
    private var mLastTouchX = 0f
    private var mLastTouchY = 0f
    private var mActivePointerId = INVALID_POINTER_ID
    private val mScaleDetector: ScaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private var mScaleFactor = 1f
    private var pivotPointX = 0f
    private var pivotPointY = 0f

    companion object {
        private const val INVALID_POINTER_ID = -1
        private const val LOG_TAG = "ZoomImageView"
    }

    init {
        mScaleDetector
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        mScaleDetector.onTouchEvent(ev)

        when (ev.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y

                mLastTouchX = x
                mLastTouchY = y

                mActivePointerId = ev.getPointerId(0)
            }

            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = ev.findPointerIndex(mActivePointerId)
                val x = ev.getX(pointerIndex)
                val y = ev.getY(pointerIndex)

                if (!mScaleDetector.isInProgress) {
                    val dx = x - mLastTouchX
                    val dy = y - mLastTouchY

                    mPosX += dx
                    mPosY += dy

                    invalidate()
                }

                mLastTouchX = x
                mLastTouchY = y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mActivePointerId = INVALID_POINTER_ID
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = (ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK) shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                val pointerId = ev.getPointerId(pointerIndex)
                if (pointerId == mActivePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mLastTouchX = ev.getX(newPointerIndex)
                    mLastTouchY = ev.getY(newPointerIndex)
                    mActivePointerId = ev.getPointerId(newPointerIndex)
                }
            }
        }

        return true
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width - 1f, height - 1f, backgroundPaint)
        drawable?.let {
            canvas.save()
            canvas.translate(mPosX, mPosY)

            val matrix = Matrix().apply {
                postScale(mScaleFactor, mScaleFactor, pivotPointX, pivotPointY)
            }

            canvas.drawBitmap((drawable as BitmapDrawable).bitmap, matrix, null)
            canvas.restore()
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        drawable?.let {
            val width = it.intrinsicWidth
            val height = it.intrinsicHeight

            val borderWidth = 0
            mScaleFactor = minOf(
                (layoutParams.width - borderWidth).toFloat() / width,
                (layoutParams.height - borderWidth).toFloat() / height
            )

            pivotPointX = ((layoutParams.width - borderWidth) - (width * mScaleFactor)) / 2
            pivotPointY = ((layoutParams.height - borderWidth) - (height * mScaleFactor)) / 2
            super.setImageDrawable(it)
        }
    }

    private fun adjustImageScaling(bitmap: Bitmap) {
        val viewWidth = width
        val viewHeight = height
        val drawableWidth = bitmap.width
        val drawableHeight = bitmap.height

        mLastTouchX = mPosX
        mLastTouchY = mPosY

        val borderWidth = 0

        mScaleFactor = if (drawableWidth <= drawableHeight) {
            (viewWidth - borderWidth).toFloat() / drawableWidth
        } else {
            (viewHeight - borderWidth).toFloat() / drawableHeight
        }

        pivotPointX = ((viewWidth - borderWidth) - (drawableWidth * mScaleFactor)) / 2
        pivotPointY = ((viewHeight - borderWidth) - (drawableHeight * mScaleFactor)) / 2

        invalidate()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        bm?.let {
            super.setImageBitmap(bm)
            adjustImageScaling(bm)
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor
            pivotPointX = detector.focusX
            pivotPointY = detector.focusY

            Log.d(LOG_TAG, "mScaleFactor $mScaleFactor")
            Log.d(LOG_TAG, "pivotPointY $pivotPointY, pivotPointX= $pivotPointX")

            mScaleFactor = maxOf(0.05f, mScaleFactor)

            invalidate()
            return true
        }
    }
}