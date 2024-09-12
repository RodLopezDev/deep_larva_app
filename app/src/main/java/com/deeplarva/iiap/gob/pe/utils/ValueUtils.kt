package com.deeplarva.iiap.gob.pe.utils

class ValueUtils {
    companion object {
        fun isInteger(str: String): Boolean {
            return str.toIntOrNull() != null
        }
    }
}