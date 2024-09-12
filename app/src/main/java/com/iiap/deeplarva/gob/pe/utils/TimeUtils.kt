package com.iiap.deeplarva.gob.pe.utils

import java.text.SimpleDateFormat
import java.util.Locale

class TimeUtils {
    companion object {
        fun formatDuration(milliseconds: Long): String {
            val totalSeconds = milliseconds / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        fun longFormatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(timestamp)
        }
    }
}