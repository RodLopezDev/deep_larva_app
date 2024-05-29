package com.rodrigo.deeplarva.routes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.rodrigo.deeplarva.services.OnServiceListener
import com.rodrigo.deeplarva.services.PredictionBoundService
import com.rodrigo.deeplarva.services.PredictionBroadcastReceiver
import com.rodrigo.deeplarva.services.PredictionService

open class BoundedActivity: AppCompatActivity(), OnServiceListener {

    private var percentage = 0
    private var receiver = PredictionBroadcastReceiver(this)
    private var boundService = PredictionBoundService(this, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver.register {
            percentage = it
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

    protected fun launchService(subSampleId: Long){
        var intent = Intent(applicationContext, PredictionService::class.java)
        intent.putExtra("subSampleId", subSampleId)
        Toast.makeText(applicationContext, "Service launched", Toast.LENGTH_SHORT).show()
        startService(intent)
    }

    protected fun isServiceRunning(): Boolean {
        return boundService.isRunning()
    }

    protected fun isServiceBounded(): Boolean {
        return boundService.isBounded()
    }

    protected fun servicePercentage(): Int {
        return percentage
    }

    override fun onStartService() {
    }

    override fun onFinishService() {
    }
}