package com.deeplarva.iiap.gob.pe.routes.activity.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.deeplarva.iiap.gob.pe.R
import com.deeplarva.iiap.gob.pe.domain.entity.Picture
import com.deeplarva.iiap.gob.pe.routes.service.PredictionService
import com.deeplarva.iiap.gob.pe.routes.service.ServiceChangesListener
import com.deeplarva.iiap.gob.pe.routes.service.binder.IBoundService
import com.deeplarva.iiap.gob.pe.routes.service.binder.PredictionBoundService
import com.deeplarva.iiap.gob.pe.routes.service.broadcast.PredictionBroadcastReceiver

open class BoundedActivity(): AppCompatActivity(), ServiceChangesListener, IBoundService {

    private var receiver = PredictionBroadcastReceiver(this)
    private var boundService = PredictionBoundService(this, this)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver.register { pictureId, percentage -> run {
            when (percentage) {
                0 -> {
                    onStartService(pictureId)
                }
                100 -> {
                    onEndService()
                }
            }
        }}
    }

    override fun onStart() {
        super.onStart()
        boundService.bind()
    }

    override fun onResume() {
        super.onResume()
        if(!boundService.isBounded()) {
            boundService.bind()
        }
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
        if(boundService.isBounded() && this.hasPictureId() != null) {
            Toast.makeText(this@BoundedActivity, getString(R.string.msg_running_yet), Toast.LENGTH_SHORT).show()
            return
        }

        var intent = Intent(applicationContext, PredictionService::class.java)
        intent.putExtra("pictureId", picture.id)
        Toast.makeText(applicationContext, getString(R.string.msg_running_prediction), Toast.LENGTH_SHORT).show()
        startService(intent)
    }

    protected fun hasPictureId(): Long? {
        return boundService.hasPictureId()
    }

    override fun onBindToService() {}

    override fun onUnBindToService() {}

    override fun onStartService(pictureId: Long) {}

    override fun onEndService() {}
}