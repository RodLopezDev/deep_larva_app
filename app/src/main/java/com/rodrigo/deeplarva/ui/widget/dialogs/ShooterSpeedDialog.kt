package com.rodrigo.deeplarva.ui.widget.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.rodrigo.deeplarva.R

class ShooterSpeedDialog(
    private val title: String = "",
    private val onValueChanged: (Int) -> Unit
) : DialogFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val dialogView = it.layoutInflater.inflate(R.layout.dialog_shooter_speed, null)
            val btnAuto = dialogView.findViewById<Button>(R.id.btnAuto)
            val btn150 = dialogView.findViewById<Button>(R.id.btn1_50)
            val btn160 = dialogView.findViewById<Button>(R.id.btn1_60)
            val btn180 = dialogView.findViewById<Button>(R.id.btn1_80)
            val btn1100 = dialogView.findViewById<Button>(R.id.btn1_100)

            btnAuto.setOnClickListener {
                onValueChanged(0)
                dialog!!.dismiss()
            }
            btn150.setOnClickListener {
                onValueChanged(50)
                dialog!!.dismiss()
            }
            btn160.setOnClickListener {
                onValueChanged(60)
                dialog!!.dismiss()
            }
            btn180.setOnClickListener {
                onValueChanged(80)
                dialog!!.dismiss()
            }
            btn1100.setOnClickListener {
                onValueChanged(100)
                dialog!!.dismiss()
            }

            val builder = AlertDialog.Builder(it)
            builder.setTitle(title)
                .setView(dialogView)
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}