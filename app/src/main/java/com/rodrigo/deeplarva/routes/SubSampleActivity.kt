package com.rodrigo.deeplarva.routes

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.databinding.ActivitySubsamplesBinding
import com.rodrigo.deeplarva.domain.view.SubSampleItemList
import com.rodrigo.deeplarva.infraestructure.DbBuilder
import com.rodrigo.deeplarva.infraestructure.driver.AppDatabase
import com.rodrigo.deeplarva.routes.services.SubSampleServices
import com.rodrigo.deeplarva.ui.adapter.SubSampleAdapterList
import com.rodrigo.deeplarva.ui.listener.ListEventListener
import com.rodrigo.deeplarva.utils.Notifications

class SubSampleActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var services: SubSampleServices
    private lateinit var lvSubSample: ListView

    private lateinit var binding: ActivitySubsamplesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubsamplesBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Sub-Muestras"

        binding.fab.setOnClickListener { view ->
            services.save {
                services.findAll { subSamples -> loadSubsamplesUI(subSamples) }
            }
        }

        lvSubSample = binding.lvSubsample

        db = DbBuilder.getInstance(this)
        services = SubSampleServices(db)

        services.findAll { subSamples -> loadSubsamplesUI(subSamples) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.subsample, menu)
        return true
    }

    private fun loadSubsamplesUI(viewSubSample: List<SubSampleItemList>) {
        val adapter = SubSampleAdapterList(this, viewSubSample, object: ListEventListener<SubSampleItemList> {
            override fun onLongClick(item: SubSampleItemList, position: Int) {
                Notifications.SigleSnackbar(binding.lvSubsample, "Long Click")
            }
            override fun onClick(item: SubSampleItemList, position: Int) {
                val intent = Intent(applicationContext, PicturesActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                intent.putExtra("subSampleId", item.id)
                applicationContext.startActivity(intent, )
            }
        })
        lvSubSample.adapter = adapter
    }
}