package com.iiap.deeplarva.ui.widget.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class SelectableDialog(
    private val items: List<String>,
    private val defaultIndexValue: Int?,
    private val title: String = "OK",
    private val okButtonText: String = "OK",
    private val cancelButtonText: String = "Cancel",
    private val onItemSelected: (Int) -> Unit
) : DialogFragment() {

    private var index: Int = defaultIndexValue ?: -1

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(title)

            // Set up the list of items with a single choice mode
            val initial = defaultIndexValue  ?: -1
            builder.setSingleChoiceItems(items.toTypedArray(), initial) { _, which ->
                index = which
            }

            // Set up the OK button
            builder.setPositiveButton(okButtonText) { dialog, _ ->
                index?.let { selected ->
                    onItemSelected(selected)
                }
                dialog.dismiss()
            }

            // Set up the Cancel button
            builder.setNegativeButton(cancelButtonText) { dialog, _ ->
                dialog.cancel()
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}