package com.rodrigo.deeplarva.routes

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.rodrigo.deeplarva.databinding.ActivityPicturesBinding
import com.rodrigo.deeplarva.domain.Constants
import com.rodrigo.deeplarva.infraestructure.DbBuilder
import com.rodrigo.deeplarva.infraestructure.driver.AppDatabase
import com.rodrigo.deeplarva.routes.observables.PictureActivityViewModel
import com.rodrigo.deeplarva.routes.services.PicturesServices
import com.rodrigo.deeplarva.routes.view.PictureActivityView
import com.rodrigo.deeplarva.routes.view.PictureViewListener
import com.rodrigo.deeplarva.utils.BitmapUtils
import com.rodrigo.deeplarva.utils.ImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PicturesActivity: BoundedActivity()  {
    private lateinit var view: PictureActivityView
    private lateinit var binding: ActivityPicturesBinding

    private lateinit var db: AppDatabase
    private lateinit var pictureService: PicturesServices
    private lateinit var viewModel: PictureActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPicturesBinding.inflate(layoutInflater)
        db = DbBuilder.getInstance(this)

        pictureService = PicturesServices(db)

        view = PictureActivityView(this, binding)
        viewModel = ViewModelProvider(this)[PictureActivityViewModel::class.java]

        view.addViewListener(object: PictureViewListener {
            override fun onPredict() {
                if(!isServiceBounded()) {
                    Toast.makeText(applicationContext, Constants.MESSAGE_SERVICE_DISCONNECTED, Toast.LENGTH_SHORT).show()
                    return
                }
                if(isServiceRunning()){
                    Toast.makeText(applicationContext, Constants.MESSAGE_SERVICE_RUNNING, Toast.LENGTH_SHORT).show()
                    return
                }
                launchService()
            }
            override fun onAddPicture() {
                view.getDialog().show()
            }
        })
        viewModel.pictures.observe(this) {
            view.loadPictures(it)
        }

        loadPictures()
    }

    fun loadPictures() {
        pictureService.findAll {
                pictures -> viewModel.updatePictures(pictures)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        view.getDialog().hide()

        if(data == null || resultCode == 0) return
        var bitmap = view.getDialog().resolve(requestCode, resultCode, data)

        GlobalScope.launch {
            var thumbnail = ImageProcessor.scale(bitmap)

            var bitmapFileName = BitmapUtils.getRandomBitmapName()
            var thumbnailFileName = BitmapUtils.getRandomBitmapName()

            val filePath = BitmapUtils.saveBitmapToStorage(applicationContext, bitmap, bitmapFileName)
            val thumbnailPath = BitmapUtils.saveBitmapToStorage(applicationContext, thumbnail, thumbnailFileName)

            withContext(Dispatchers.Main) {
                if (filePath == null) {
                    Toast.makeText(applicationContext, Constants.MESSAGE_ERROR_LOADING_IMAGE, Toast.LENGTH_LONG).show()
                    return@withContext
                }
                if (thumbnailPath == null) {
                    Toast.makeText(applicationContext, Constants.MESSAGE_ERROR_LOADING_IMAGE, Toast.LENGTH_LONG).show()
                    return@withContext
                }
                pictureService.save(filePath, thumbnailPath) {
                    pictureService.findAll {
                        viewModel.updatePictures(it)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        view.onRequestCameraResult(requestCode, permissions, grantResults)
    }

    override fun onEndService() {
        super.onEndService()
        loadPictures()
    }
}