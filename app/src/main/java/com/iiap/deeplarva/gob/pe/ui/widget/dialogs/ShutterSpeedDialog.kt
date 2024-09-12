package com.iiap.deeplarva.gob.pe.ui.widget.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.iiap.deeplarva.gob.pe.R
import com.iiap.deeplarva.gob.pe.domain.constants.ConfigConstants
import com.iiap.deeplarva.gob.pe.domain.constants.MessagesConstants
import com.iiap.deeplarva.gob.pe.utils.PreferencesHelper

class ShutterSpeedDialog(
    private val preferencesHelper: PreferencesHelper,
    private val title: String = "",
    private val initialValue: Int = 0,
    private val onValueChanged: (Int, String) -> Unit
) : DialogFragment() {
    private val MIN_VALUE = 0
    private val MAX_VALUE = 1000
    private var enabled = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val dialogView = it.layoutInflater.inflate(R.layout.dialog_shutter_speed, null)
            val btnOff = dialogView.findViewById<Button>(R.id.btnOff)
            val btn150 = dialogView.findViewById<Button>(R.id.btn1_50)
            val btn160 = dialogView.findViewById<Button>(R.id.btn1_60)
            val btn180 = dialogView.findViewById<Button>(R.id.btn1_80)
            val btn1100 = dialogView.findViewById<Button>(R.id.btn1_100)
            val btn11000 = dialogView.findViewById<Button>(R.id.btn1_1000)
            val etManual = dialogView.findViewById<EditText>(R.id.etManual)

            btnOff.setOnClickListener {
                onValueChanged(0, MessagesConstants.DEFAULT_VALUE_SHUTTER_SPEED)
                dialog!!.dismiss()
            }
            btn150.setOnClickListener {
                onValueChanged(20, "1 / 50") // 1000 / 50
                dialog!!.dismiss()
            }
            btn160.setOnClickListener {
                onValueChanged(17, "1 / 60") // 1000 / 60
                dialog!!.dismiss()
            }
            btn180.setOnClickListener {
                onValueChanged(13, "1 / 80") // 1000 / 80
                dialog!!.dismiss()
            }
            btn1100.setOnClickListener {
                onValueChanged(10, "1 / 100") // 1000 / 100
                dialog!!.dismiss()
            }
            btn11000.setOnClickListener {
                onValueChanged(1, "1") // 1000 / 1000
                dialog!!.dismiss()
            }

            etManual.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(editable: Editable?) {
                    val input = editable.toString()
                    if (input.isNotEmpty()) {
                        try {
                            val value = input.toInt()
                            enabled = false
                            if (value < MIN_VALUE) {
                                etManual.error = "Value must be at least $MIN_VALUE"
                            } else if (value > MAX_VALUE) {
                                etManual.error = "Value must not exceed $MAX_VALUE"
                            } else {
                                enabled = true
                            }
                        } catch (e: NumberFormatException) {
                            etManual.error = "Invalid input"
                        }
                    }
                }
            })


            val showEt = preferencesHelper.getBoolean(ConfigConstants.CONFIG_SHOW_SHUTTER_SPEED_CUSTOM)
            if(showEt) {
                etManual.setText(initialValue.toString())
                etManual.visibility = View.VISIBLE

                btn150.visibility = View.GONE
                btn160.visibility = View.GONE
                btn180.visibility = View.GONE
                btn11000.visibility = View.GONE
            }

            val builder = AlertDialog.Builder(it)
            builder.setTitle(title)
                .setView(dialogView)
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }

            if(showEt) {
                builder.setPositiveButton("Yes") { dialog, which ->
                    val value = etManual.text.toString().toIntOrNull()
                    if(value != null && enabled) {
                        onValueChanged(value, "${value.toString()} ms")
                        dialog!!.dismiss()
                    }
                }
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}