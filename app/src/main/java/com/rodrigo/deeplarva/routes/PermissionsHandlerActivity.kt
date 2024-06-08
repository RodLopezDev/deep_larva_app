package com.rodrigo.deeplarva.routes

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.domain.Constants

class PermissionsHandlerActivity: AppCompatActivity() {

    private val REQUESTCODE = 1
    private lateinit var tvText: TextView
    private lateinit var btnRun: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        setContentView(R.layout.activity_permissions)
        btnRun = findViewById(R.id.btnPermissions)
        tvText = findViewById(R.id.tvPermissionsText)

        btnRun.setOnClickListener {
            requestPermissions()
        }

        tvText.visibility = View.INVISIBLE
        btnRun.visibility = View.INVISIBLE
    }

    private fun requestPermissions() {
        val requiredPermissions = Constants.APP_PERMISSIONS_LIST.filter {
            ContextCompat.checkSelfPermission(this@PermissionsHandlerActivity, it)  != PackageManager.PERMISSION_GRANTED
        }

        if(requiredPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, requiredPermissions.toTypedArray(), REQUESTCODE)
            return
        }

        initApp()
    }

    private fun initApp() {
        var intent = Intent(this, PicturesActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val rejectedPermissions = permissions.withIndex().filter {
            (index, it) -> grantResults[index] != PackageManager.PERMISSION_GRANTED
        }.map { it.value }

        if(rejectedPermissions.isEmpty()) {
            initApp()
            return
        }

        tvText.visibility = View.VISIBLE
        btnRun.visibility = View.VISIBLE
        val rejectedText = rejectedPermissions.joinToString(separator = "\n")
        tvText.text = "Required permissions:\n\n$rejectedText\n\nIf request does not show, go to device config and configure permissions for app manually."
    }
}