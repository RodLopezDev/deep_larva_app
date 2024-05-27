package com.odrigo.recognitionappkt.view

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.odrigo.recognitionappkt.R
import com.odrigo.recognitionappkt.adapters.SubSampleAdapterList
import com.odrigo.recognitionappkt.db.AppDatabase
import com.odrigo.recognitionappkt.db.BDFactory
import com.odrigo.recognitionappkt.domain.SubSample
import com.odrigo.recognitionappkt.domain.views.SubSampleItemList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    private lateinit var lvSamples: ListView
    private lateinit var btnNewSubsample: FloatingActionButton

    private val subSampleListView = SubSampleListView(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subsample_list)
        lvSamples = findViewById<ListView>(R.id.lv_subsamples)
        btnNewSubsample = findViewById<FloatingActionButton>(R.id.btn_new_subsample)

        supportActionBar?.title = "Sub-Muestras"
        db = BDFactory.getInstance(this)

        btnNewSubsample.setOnClickListener {
            NewSubsample()
        }

        loadPictures()
    }

    private fun loadPictures(){
        GlobalScope.launch {
            var subSamples = db.subSample().getAllSubSamplesForUIList()
            withContext(Dispatchers.Main) {
                loadSubsamplesUI(subSamples)
            }
        }
    }

    private fun NewSubsample(){
        GlobalScope.launch {
            db.subSample().insert(SubSample(isTraining = false, max = 0, mean = 0, min = 0))
            withContext(Dispatchers.Main) {
                loadPictures()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadPictures()
    }

    private fun loadSubsamplesUI(viewSubSample: List<SubSampleItemList>) {
        val adapter = SubSampleAdapterList(this, Transformers.SubSampleToDeletable(viewSubSample), subSampleListView)
        lvSamples.adapter = adapter
    }
}