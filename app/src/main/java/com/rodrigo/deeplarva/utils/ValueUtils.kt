package com.rodrigo.deeplarva.utils

class ValueUtils {
    companion object {
        fun isInteger(str: String): Boolean {
            return str.toIntOrNull() != null
        }
    }
}