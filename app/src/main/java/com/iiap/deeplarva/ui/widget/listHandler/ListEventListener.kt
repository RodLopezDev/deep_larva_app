package com.iiap.deeplarva.ui.widget.listHandler

interface ListEventListener<T> {
    fun onClick(item: T, position: Int)
    fun onLongClick(item: T, position: Int)
}