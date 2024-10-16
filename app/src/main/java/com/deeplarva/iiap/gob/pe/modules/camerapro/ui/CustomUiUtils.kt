package com.deeplarva.iiap.gob.pe.modules.camerapro.ui

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.kylecorry.andromeda.alerts.Alerts
import com.kylecorry.andromeda.core.system.Resources
import com.deeplarva.iiap.gob.pe.R
import com.deeplarva.iiap.gob.pe.modules.camerapro.view.DurationInputView
import java.time.Duration

object CustomUiUtils {

    @RequiresApi(Build.VERSION_CODES.O)
    fun pickDuration(
        context: Context,
        default: Duration? = null,
        title: String,
        message: String? = null,
        hint: String? = null,
        showSeconds: Boolean = false,
        onDurationPick: (duration: Duration?) -> Unit
    ) {
        val view = View.inflate(context, R.layout.view_duration_entry_prompt, null)
        var duration: Duration? = default
        val durationMessage = view.findViewById<TextView>(R.id.prompt_duration_message)
        val durationInput = view.findViewById<DurationInputView>(R.id.prompt_duration)

        durationMessage.isVisible = !message.isNullOrBlank()
        durationMessage.text = message

        durationInput.showSeconds = showSeconds

        if (!hint.isNullOrBlank()) {
            durationInput.hint = hint
        }

        durationInput?.setOnDurationChangeListener {
            duration = it
        }
        durationInput?.updateDuration(default)

        Alerts.dialog(
            context,
            title,
            contentView = view
        ) { cancelled ->
            if (cancelled) {
                onDurationPick.invoke(null)
            } else {
                onDurationPick.invoke(duration)
            }
        }
    }

    fun Resources.getPrimaryColor(context: Context): Int {
        return getAndroidColorAttr(context, com.google.android.material.R.attr.colorPrimary)
    }

}