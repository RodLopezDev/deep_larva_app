package com.deeplarva.iiap.gob.pe.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class UserUtils {
    companion object {
        fun copyTextToClipboard(context: Context,text: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", text)
            clipboard.setPrimaryClip(clip)
        }
    }
}