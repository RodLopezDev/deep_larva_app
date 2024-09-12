package com.deeplarva.iiap.gob.pe.ui.widget.listHandler

interface ListEventListener<T> {
    fun onClick(item: T, position: Int)
    fun onLongClick(item: T, position: Int)
}