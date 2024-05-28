package com.rodrigo.deeplarva.routes

import android.content.Intent
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
import com.rodrigo.deeplarva.domain.entity.SubSample
import com.rodrigo.deeplarva.infraestructure.DbBuilder
import com.rodrigo.deeplarva.infraestructure.driver.AppDatabase
import com.rodrigo.deeplarva.routes.observables.PictureActivityViewModel
import com.rodrigo.deeplarva.routes.ui.gallery.GalleryViewModel
import com.rodrigo.deeplarva.routes.view.AddPictureDialog
import com.rodrigo.deeplarva.routes.view.PictureActivityView
import com.rodrigo.deeplarva.routes.view.PictureViewListener
import com.rodrigo.deeplarva.services.PicturesServices
import com.rodrigo.deeplarva.services.SubSampleServices
import com.rodrigo.deeplarva.ui.adapter.PictureRecyclerViewAdapter
import com.rodrigo.deeplarva.utils.BitmapUtils
import com.rodrigo.deeplarva.utils.ImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PicturesActivity: AppCompatActivity()  {

    private var subSampleId: Long = 0

    private lateinit var view: PictureActivityView
    private lateinit var binding: ActivityPicturesBinding

    private lateinit var db: AppDatabase
    private lateinit var pictureService: PicturesServices
    private lateinit var subsampleService: SubSampleServices
    private lateinit var viewModel: PictureActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subSampleId = intent.getLongExtra("subSampleId", 0)
        binding = ActivityPicturesBinding.inflate(layoutInflater)
        db = DbBuilder.getInstance(this)

        pictureService = PicturesServices(db)
        subsampleService = SubSampleServices(db)

        view = PictureActivityView(this, binding, subSampleId)
        viewModel = ViewModelProvider(this)[PictureActivityViewModel::class.java]

        view.addViewListener(object: PictureViewListener {
            override fun onPredict() {
            }
            override fun onAddPicture() {
                view.getDialog().show()
            }
        })
        viewModel.subSample.observe(this) {
            loadPictures(it)
        }
        viewModel.pictures.observe(this) {
            view.loadPictures(it)
        }

        loadSubSample(subSampleId)
    }

    fun loadPictures(subSample: SubSample?) {
        if (subSample != null)
            pictureService.findBySubSampleId(subSample.id) {
                    pictures -> viewModel.updatePictures(pictures)
            }
    }

    fun loadSubSample(subSampleId: Long) {
        subsampleService.findOne(subSampleId) { subSample -> run {
            if (subSample == null) {
                Toast.makeText(this, "SubSample Not Found", Toast.LENGTH_SHORT).show()
                finish()
                return@run
            }
            viewModel.updateSubSample(subSample)
        }}
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data == null) return
        view.getDialog().hide()

        GlobalScope.launch {
            var bitmap = view.getDialog().resolve(requestCode, resultCode, data)
            var thumbnail = ImageProcessor.scale(bitmap)

            var bitmapFileName = BitmapUtils.getRandomBitmapName()
            var thumbnailFileName = BitmapUtils.getRandomBitmapName()

            val filePath = BitmapUtils.saveBitmapToStorage(applicationContext, bitmap, bitmapFileName)
            val thumbnailPath = BitmapUtils.saveBitmapToStorage(applicationContext, thumbnail, thumbnailFileName)

            withContext(Dispatchers.Main) {
                if (filePath == null) {
                    Toast.makeText(applicationContext, "ERROR AL CARGAR IMAGEN", Toast.LENGTH_LONG).show()
                    return@withContext
                }
                if (thumbnailPath == null) {
                    Toast.makeText(applicationContext, "ERROR AL CARGAR IMAGEN", Toast.LENGTH_LONG).show()
                    return@withContext
                }
                pictureService.save(subSampleId, filePath, thumbnailPath) {
                    pictureService.findBySubSampleId(subSampleId) {
                            pictures -> viewModel.updatePictures(pictures)
                    }
                }
            }
        }

    }
}