package com.rodrigo.deeplarva.ui.camera

import android.graphics.SurfaceTexture
import android.view.TextureView
import com.rodrigo.deeplarva.routes.CameraActivity

class CameraSurfaceTextureListener(private var activity: CameraEventsListener): TextureView.SurfaceTextureListener {
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        activity.initCamera()
    }
    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return true
    }
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }
}