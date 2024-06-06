package com.rodrigo.deeplarva.routes.camera

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.view.MotionEvent
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.routes.camera.interfaces.CameraOnTouchListener

class CameraTextureView(private val activity: AppCompatActivity, private val listener: CameraOnTouchListener) {

    private lateinit var imageReader: ImageReader
    private val textureView: TextureView = activity.findViewById(R.id.textureView)

    @SuppressLint("MissingPermission")
    fun render(camera: Camera, handler: Handler){
        imageReader = ImageReader.newInstance(camera.largest.width, camera.largest.height, ImageFormat.JPEG, 3)
        imageReader.setOnImageAvailableListener(ImageReader.OnImageAvailableListener { reader ->
            val image: Image? = reader.acquireLatestImage()
            image?.let {
//                val buffer: ByteBuffer = it.planes[0].buffer
//                val bytes = ByteArray(buffer.capacity())
//                buffer.get(bytes)
////                saveImage(bytes)
//                it.close()
            }
        }, handler)

        if(textureView.isAvailable) {
            camera.openCamera(handler)
            return
        }

        textureView.surfaceTextureListener =  object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                camera.openCamera(handler)
            }
            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                return true
            }
        }

        textureView.setOnTouchListener { _, event ->
//            scaleGestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_DOWN) {
                listener.onTouch(event.x, event.y)
            }
            true
        }
    }

    fun getTextureView(): TextureView {
        return textureView
    }

    fun getImageReader(): ImageReader {
        return imageReader
    }
}