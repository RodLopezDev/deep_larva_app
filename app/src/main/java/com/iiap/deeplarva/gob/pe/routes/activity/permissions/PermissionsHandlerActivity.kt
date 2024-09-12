package com.iiap.deeplarva.gob.pe.routes.activity.permissions

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.iiap.deeplarva.gob.pe.R
import com.iiap.deeplarva.gob.pe.domain.constants.PermissionsConstans
import com.iiap.deeplarva.gob.pe.routes.activity.main.PicturesActivity

class PermissionsHandlerActivity: AppCompatActivity() {

    private val REQUEST_CODE = 1
    private lateinit var tvText: TextView
    private lateinit var btnRun: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)
        btnRun = findViewById(R.id.btnPermissions)
        tvText = findViewById(R.id.tvPermissionsText)

        btnRun.setOnClickListener {
            requestPermissions()
        }

        tvText.visibility = View.INVISIBLE
        btnRun.visibility = View.INVISIBLE

        requestPermissions()
    }

    private fun requestPermissions() {
        val requiredPermissions = PermissionsConstans.getPermissionsList().filter {
            val permission = ContextCompat.checkSelfPermission(this@PermissionsHandlerActivity, it)
            permission != PackageManager.PERMISSION_GRANTED
        }

        if(requiredPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, requiredPermissions.toTypedArray(), REQUEST_CODE)
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