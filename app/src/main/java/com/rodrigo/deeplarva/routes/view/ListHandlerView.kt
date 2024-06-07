package com.rodrigo.deeplarva.routes.view

import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView

class ListHandlerView<T>(private var list: ListView, private var tvtext: TextView) {
    fun populate(items: List<T>, adapter: ArrayAdapter<T>) {
        if(items.isEmpty()){
            tvtext.visibility = View.VISIBLE
            list.visibility = View.INVISIBLE
            return
        }
        list.adapter = adapter
        tvtext.visibility = View.INVISIBLE
        list.visibility = View.VISIBLE
    }
}