package com.rodrigo.deeplarva.routes

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rodrigo.deeplarva.databinding.ActivityPicturesBinding
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.infraestructure.DbBuilder
import com.rodrigo.deeplarva.infraestructure.driver.AppDatabase
import com.rodrigo.deeplarva.routes.observables.PictureActivityViewModel
import com.rodrigo.deeplarva.routes.ui.gallery.GalleryViewModel
import com.rodrigo.deeplarva.routes.view.PictureActivityView
import com.rodrigo.deeplarva.services.PicturesServices
import com.rodrigo.deeplarva.services.SubSampleServices
import com.rodrigo.deeplarva.ui.adapter.PictureRecyclerViewAdapter

class PicturesActivity: AppCompatActivity()  {

    private lateinit var view: PictureActivityView
    private lateinit var binding: ActivityPicturesBinding

    private lateinit var db: AppDatabase
    private lateinit var pictureService: PicturesServices
    private lateinit var subsampleService: SubSampleServices

    private lateinit var gridLayoutManager: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val subSampleId = intent.getLongExtra("subSampleId", 0)

        binding = ActivityPicturesBinding.inflate(layoutInflater)
        view = PictureActivityView(this, binding, subSampleId)

        db = DbBuilder.getInstance(this)
        pictureService = PicturesServices(db)
        subsampleService = SubSampleServices(db)

        gridLayoutManager = GridLayoutManager(this, 2)

        val viewModel = ViewModelProvider(this)[PictureActivityViewModel::class.java]
        viewModel.subSample.observe(this) {
            if (it != null)
                pictureService.findBySubSampleId(it.id, ::loadPictures)
        }
        viewModel.pictures.observe(this) {
            loadPictures(it)
        }

        subsampleService.findOne(subSampleId) {
            subSample -> run {
                if (subSample == null) {
                    Toast.makeText(this, "SubSample Not Found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@run
                }
                viewModel.updateSubSample(subSample)
            }
        }
    }

    fun loadPictures(pictures: List<Picture>) {
        val adapter = PictureRecyclerViewAdapter(pictures)
        binding.rvPictures.adapter = adapter
        binding.rvPictures.layoutManager = gridLayoutManager
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}