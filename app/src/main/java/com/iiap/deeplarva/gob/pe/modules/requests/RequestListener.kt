package com.iiap.deeplarva.modules.requests

interface RequestListener<T> {
    fun onComplete(result: T)
    fun onFailure()
}