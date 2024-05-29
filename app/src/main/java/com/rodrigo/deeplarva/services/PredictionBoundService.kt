package com.rodrigo.deeplarva.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity

class PredictionBoundService(private val activity:AppCompatActivity) {

    private lateinit var predictionService: PredictionService
    private var mBound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PredictionService.LocalBinder
            predictionService = binder.getService()
            
            mBound = true
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }
    
    fun bind() {
        Intent(activity, PredictionService::class.java).also { intent ->
            activity.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbind() {
        activity.unbindService(connection)
        mBound = false
    }

    fun isRunning(): Boolean {
        return predictionService.isRunning
    }

    fun isBounded (): Boolean {
        return mBound
    }
}