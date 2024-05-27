package com.odrigo.recognitionappkt.view

import android.content.Context
import android.content.Intent
import com.odrigo.recognitionappkt.domain.views.SubSampleItemList
import com.odrigo.recognitionappkt.routes.pictures.Presenter

class SubSampleListView(private var context: Context): ActivityChangesListener<SubSampleItemList> {

    private var modeDelete = false

    override fun modeDelete(): Boolean {
        return modeDelete
    }

    override fun setOnLongClickListener(position: Int) {
    }

    override fun setOnClickListener(item: SubSampleItemList, position: Int) {
        if (modeDelete){

            return
        }
        val intent = Intent(context, Presenter::class.java)
        intent.putExtra("subSampleId", item.id)
        context.startActivity(intent)
    }
}