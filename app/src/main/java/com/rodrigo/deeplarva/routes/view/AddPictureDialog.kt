package com.rodrigo.deeplarva.routes.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.utils.image.ImageHandler

class AddPictureDialog(private val context: AppCompatActivity) {

    private val handler: ImageHandler
    private var dialog: AlertDialog

    private var dialogView: View

    init {
        handler = ImageHandler(context)
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_picture, null)

        dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Nueva imagen")
            .create()

        val btnLoadPic: Button = dialogView.findViewById(R.id.btn_load_pic)
        val btnCamera: Button = dialogView.findViewById(R.id.btn_camera)

        btnLoadPic.setOnClickListener { handler.launchStorage() }
        btnCamera.setOnClickListener { handler.launchCamera() }
    }

    fun show() {
        dialog?.show()
    }

    fun hide() {
        dialog?.dismiss()
    }

    fun resolve(requestCode: Int, resultCode: Int, data: Intent?): Bitmap {
        return handler.resolve(requestCode, resultCode, data)
    }
}