package com.rodrigo.deeplarva.modules.requests

interface RequestListener {
    fun onComplete()
    fun onFailure()
}