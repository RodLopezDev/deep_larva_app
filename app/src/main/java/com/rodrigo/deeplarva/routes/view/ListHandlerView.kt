package com.rodrigo.deeplarva.routes.view

import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.ui.listener.ListEventListener

class ListHandlerView<T>(private var list: ListView, private var tvtext: TextView, private val listener: ListEventListener<T>) {
    fun populate(items: List<T>, adapter: ArrayAdapter<T>) {
        if(items.isEmpty()){
            tvtext.visibility = View.VISIBLE
            list.visibility = View.INVISIBLE
            return
        }
        list.adapter = adapter
        tvtext.visibility = View.INVISIBLE
        list.visibility = View.VISIBLE

        list.setOnItemClickListener { parent, view, position, id ->
            val element = items[position]
            if(element != null) {
                listener.onClick(element, position)
            }
        }
        list.setOnItemLongClickListener { parent, view, position, id ->
            val element = items[position]
            if(element != null) {
                listener.onLongClick(element, position)
            }
            true
        }
    }
}