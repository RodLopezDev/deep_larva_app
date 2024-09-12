package com.deeplarva.iiap.gob.pe.modules.requests

interface RequestListener<T> {
    fun onComplete(result: T)
    fun onFailure()
}