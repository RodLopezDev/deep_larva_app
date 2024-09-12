package com.iiap.deeplarva.gob.pe.routes.service.binder

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.iiap.deeplarva.gob.pe.routes.service.PredictionService

class PredictionBoundService(private val activity:AppCompatActivity, private val IBoundService: IBoundService) {

    private lateinit var predictionService: PredictionService
    private var mBound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PredictionService.PredictionServiceBinder
            predictionService = binder.getService()
            mBound = true
            IBoundService.onBindToService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
            IBoundService.onUnBindToService()
        }
    }

    fun bind() {
        Intent(activity, PredictionService::class.java).also { intent ->
            activity.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbind() {
        if (!mBound) return
        activity.unbindService(connection)
        mBound = false
    }

    fun hasPictureId(): Long? {
        return predictionService.pictureId
    }

    // This works jut after ServiceConnection.onServiceConnected have been executed
    fun isBounded (): Boolean {
        return mBound
    }
}