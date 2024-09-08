package com.iiap.deeplarva.ui.widget.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.iiap.deeplarva.R

class SeekDialog(
    private val minValue: Int,
    private val maxValue: Int,
    private val initialValue: Int,
    private val title: String = "",
    private val onValueChanged: (Int) -> Unit
) : DialogFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val dialogView = it.layoutInflater.inflate(R.layout.dialog_seekbar, null)
            val seekBar = dialogView.findViewById<SeekBar>(R.id.seekBar)
            val valueTextView = dialogView.findViewById<TextView>(R.id.valueTextView)

            seekBar.min = minValue
            seekBar.max = maxValue
            seekBar.progress = initialValue - minValue

            valueTextView.text = initialValue.toString()

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = minValue + progress
                    valueTextView.text = value.toString()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) { }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            val builder = AlertDialog.Builder(it)
            builder.setTitle(title)
                .setView(dialogView)
                .setPositiveButton("OK") { dialog, _ ->
                    val finalValue = minValue + seekBar.progress
                    onValueChanged(finalValue)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}