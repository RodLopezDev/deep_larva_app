package com.iiap.deeplarva.gob.pe.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class DateUtils {
    companion object {
        fun isSameAsToday(dateString: String): Boolean {
            return try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val date = LocalDate.parse(dateString, formatter)
                val today = LocalDate.now()
                date.isEqual(today)
            } catch (e: DateTimeParseException) {
                false
            }
        }
        fun getToday(): String {
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            return today.format(formatter)
        }
    }
}