package com.iiap.deeplarva.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.iiap.deeplarva.R

class ThemeUtils {
    companion object {
        fun isDarkTheme(context: Context): Boolean {
            return context.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        }
        fun getBackIconDrawable(context: Context): Drawable? {
            val drawable = ContextCompat.getDrawable(context, androidx.appcompat.R.drawable.abc_ic_ab_back_material)

            // Tint the drawable
            drawable?.let {
                DrawableCompat.setTint(it, ContextCompat.getColor(context, R.color.white))
            }
            return drawable
        }
    }
}