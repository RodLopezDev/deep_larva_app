package com.rodrigo.deeplarva.routes.camera

import android.os.Handler
import android.os.HandlerThread

class CameraThreadUpdater {
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null

    init {
        backgroundThread = HandlerThread("Camera Background").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    fun onStart(){
        if(backgroundThread == null) {
            backgroundThread = HandlerThread("Camera Background").also { it.start() }
        }
        if(backgroundHandler == null) {
            backgroundHandler = Handler(backgroundThread!!.looper)
        }
    }

    fun getHandler(): Handler {
        return backgroundHandler!!
    }
}