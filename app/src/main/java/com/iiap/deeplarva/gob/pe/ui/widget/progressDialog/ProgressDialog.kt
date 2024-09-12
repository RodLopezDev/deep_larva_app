package com.iiap.deeplarva.gob.pe.ui.widget.progressDialog

import android.app.AlertDialog
import android.content.Context
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.iiap.deeplarva.gob.pe.R

class ProgressDialog {
    private var progressDialog: AlertDialog? = null
    fun show(context: Context) {
        val builder = AlertDialog.Builder(context)

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 50, 50, 50)
        layout.addView(progressBar)
        layout.setBackgroundColor(context.getColor(R.color.dialogBg))

        builder.setView(layout)

        builder.setCancelable(false)

        progressDialog = builder.create()
        progressDialog?.show()
    }

    fun dismiss(){
        if (progressDialog != null && progressDialog?.isShowing == true) {
            progressDialog?.dismiss();
        }
    }
}