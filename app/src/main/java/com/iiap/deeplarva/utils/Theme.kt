package com.rodrigo.deeplarva.utils

import android.content.Context
import android.content.res.Configuration

class Theme {
    companion object {
        fun isDarkTheme(context: Context): Boolean {
            return context.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        }
    }
}