package com.rodrigo.deeplarva.utils

class Values {
    companion object {
        fun isInteger(str: String): Boolean {
            return str.toIntOrNull() != null
        }
    }
}