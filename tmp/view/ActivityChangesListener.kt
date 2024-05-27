package com.odrigo.recognitionappkt.view

interface ActivityChangesListener<T> {
    fun modeDelete(): Boolean
    fun setOnClickListener (item: T, position: Int)
    fun setOnLongClickListener (position: Int)
}