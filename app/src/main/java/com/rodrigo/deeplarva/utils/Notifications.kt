package com.rodrigo.deeplarva.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

class Notifications {
    companion object {
        fun SigleSnackbar (view: View, message: String) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }
}