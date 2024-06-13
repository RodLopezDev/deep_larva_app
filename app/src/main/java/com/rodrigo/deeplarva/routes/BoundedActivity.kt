package com.rodrigo.deeplarva.routes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.domain.entity.Picture

import com.rodrigo.deeplarva.services.PredictionBoundService
import com.rodrigo.deeplarva.services.PredictionBroadcastReceiver
import com.rodrigo.deeplarva.services.PredictionService
import com.rodrigo.deeplarva.services.ServiceChangesListener

open class BoundedActivity: AppCompatActivity(), ServiceChangesListener {

    private var receiver = PredictionBroadcastReceiver(this)
    private var boundService = PredictionBoundService(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver.register {
            when (it) {
                0 -> {
                    onStartService()
                }
                100 -> {
                    onEndService()
                }
                else -> {
                    updateServiceValue(it)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        boundService.bind()
    }

    override fun onStop() {
        super.onStop()
        boundService.unbind()
    }

    override fun onDestroy() {
        super.onDestroy()
        receiver.unregister()
    }

    protected fun launchService(picture: Picture){
        var intent = Intent(applicationContext, PredictionService::class.java)
        intent.putExtra("pictureId", picture.id)
        Toast.makeText(applicationContext, Constants.MESSAGE_SERVICE_STARTED, Toast.LENGTH_SHORT).show()
        startService(intent)
    }

    protected fun isServiceRunning(): Boolean {
        return boundService.isRunning()
    }

    protected fun isServiceBounded(): Boolean {
        return boundService.isBounded()
    }

    protected fun updateServiceValue(percentage: Int) {

    }

    override fun onStartService() {
    }

    override fun onEndService() {
    }
}