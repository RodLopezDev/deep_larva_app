package com.rodrigo.deeplarva.routes

import android.os.Bundle
import android.view.Menu
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.databinding.ActivitySubsamplesBinding
import com.rodrigo.deeplarva.domain.entity.SubSample
import com.rodrigo.deeplarva.domain.view.SubSampleItemList
import com.rodrigo.deeplarva.infraestructure.Builder
import com.rodrigo.deeplarva.infraestructure.driver.AppDatabase
import com.rodrigo.deeplarva.ui.adapter.SubSampleAdapterList

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubSampleActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var lvSubSample: ListView

    private lateinit var binding: ActivitySubsamplesBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubsamplesBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.appBarSubsample.toolbar)
        supportActionBar?.title = "Sub-Muestras"

        binding.appBarSubsample.fab.setOnClickListener { view ->
            eventNewSubSample()
        }

        lvSubSample = binding.appBarSubsample.lvSubsample

        db = Builder.getInstance(this)

        loadPictures()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.subsample, menu)
        return true
    }

    private fun loadPictures(){
        GlobalScope.launch {
            var subSamples = db.subSample().getAllSubSamplesForUIList()
            withContext(Dispatchers.Main) {
                loadSubsamplesUI(subSamples)
            }
        }
    }


    fun eventNewSubSample() {
        GlobalScope.launch {
            db.subSample().insert(SubSample(isTraining = false, max = 0f, mean = 0f, min = 0f, average = 0f, name = "Pruebas"))
            withContext(Dispatchers.Main) {
                loadPictures()
            }
        }
    }

    private fun loadSubsamplesUI(viewSubSample: List<SubSampleItemList>) {
        val adapter = SubSampleAdapterList(this, viewSubSample)
        lvSubSample.adapter = adapter
    }
}