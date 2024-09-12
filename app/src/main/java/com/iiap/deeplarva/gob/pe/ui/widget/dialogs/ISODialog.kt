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
import com.iiap.deeplarva.gob.pe.utils.PreferencesHelper

class ISODialog(
    private val preferencesHelper: PreferencesHelper,
    private val title: String = "",
    private val initialValue: Int = 0,
    private val onValueChanged: (Int) -> Unit
) : DialogFragment() {
    private val MIN_VALUE = 0
    private val MAX_VALUE = 3200
    private var enabled = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val dialogView = it.layoutInflater.inflate(R.layout.dialog_iso, null)
            val btnAuto = dialogView.findViewById<Button>(R.id.btnAuto)
            val btn50 = dialogView.findViewById<Button>(R.id.btn50)
            val btn100 = dialogView.findViewById<Button>(R.id.btn100)
            val btn200 = dialogView.findViewById<Button>(R.id.btn200)
            val btn400 = dialogView.findViewById<Button>(R.id.btn400)
            val btn800 = dialogView.findViewById<Button>(R.id.btn800)
            val etManual = dialogView.findViewById<EditText>(R.id.etManual)

            btnAuto.setOnClickListener {
                onValueChanged(0)
                dialog!!.dismiss()
            }
            btn50.setOnClickListener {
                onValueChanged(50)
                dialog!!.dismiss()
            }
            btn100.setOnClickListener {
                onValueChanged(100)
                dialog!!.dismiss()
            }
            btn200.setOnClickListener {
                onValueChanged(200)
                dialog!!.dismiss()
            }
            btn400.setOnClickListener {
                onValueChanged(400)
                dialog!!.dismiss()
            }
            btn800.setOnClickListener {
                onValueChanged(800)
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


            val showEt = preferencesHelper.getBoolean(ConfigConstants.CONFIG_SHOW_ISO_CUSTOM)
            if(showEt) {
                etManual.setText(initialValue.toString())
                etManual.visibility = View.VISIBLE

                btn50.visibility = View.GONE
                btn100.visibility = View.GONE
                btn200.visibility = View.GONE
                btn800.visibility = View.GONE
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
                        onValueChanged(value)
                        dialog!!.dismiss()
                    }
                }
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}