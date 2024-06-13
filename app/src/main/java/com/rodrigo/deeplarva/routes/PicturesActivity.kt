package com.rodrigo.deeplarva.routes

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.application.UseCaseSyncPicture
import com.rodrigo.deeplarva.databinding.ActivityPicturesBinding
import com.rodrigo.deeplarva.domain.Constants
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.domain.utils.BitmapProcessingResult
import com.rodrigo.deeplarva.infraestructure.DbBuilder
import com.rodrigo.deeplarva.infraestructure.driver.AppDatabase
import com.rodrigo.deeplarva.modules.requests.RequestListener
import com.rodrigo.deeplarva.routes.observables.PictureActivityViewModel
import com.rodrigo.deeplarva.routes.services.BackendPictureServices
import com.rodrigo.deeplarva.routes.services.BoxDetectionServices
import com.rodrigo.deeplarva.routes.services.PicturesServices
import com.rodrigo.deeplarva.routes.view.IPictureViewListener
import com.rodrigo.deeplarva.routes.view.PictureActivityView
import com.rodrigo.deeplarva.ui.ProgressDialog
import com.rodrigo.deeplarva.ui.adapter.PictureItemListListener
import com.rodrigo.deeplarva.utils.BitmapUtils
import com.rodrigo.deeplarva.utils.ImageProcessor
import com.rodrigo.deeplarva.utils.PreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PicturesActivity: BoundedActivity(), IPictureViewListener  {
    private lateinit var view: PictureActivityView
    private lateinit var binding: ActivityPicturesBinding

    private lateinit var db: AppDatabase
    private lateinit var pictureService: PicturesServices
    private lateinit var boxDetectionServices: BoxDetectionServices
    private lateinit var viewModel: PictureActivityViewModel

    private lateinit var deviceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPicturesBinding.inflate(layoutInflater)

        db = DbBuilder.getInstance(this)
        pictureService = PicturesServices(db)
        boxDetectionServices = BoxDetectionServices(db)
        deviceId = PreferencesHelper(this).getString(Constants.SHARED_PREFERENCES_DEVICE_ID)!!

        view = PictureActivityView(deviceId, this, binding, this)
        viewModel = ViewModelProvider(this)[PictureActivityViewModel::class.java]

        viewModel.pictures.observe(this) {
            view.loadPictures(it, object: PictureItemListListener {
                override fun onPredict(picture: Picture) {
                    launchService(picture)
                }
            })
        }

        loadPictures()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_info -> {
                view.showInfoDialog()
                true
            }
            R.id.action_sync -> {
                sync()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data == null || resultCode == 0) return

        val dialog = ProgressDialog()
        dialog.show(this@PicturesActivity)

        val bitmaps = view.resolve(requestCode, resultCode, data)
        GlobalScope.launch {
            val results = bitmaps.map {
                var thumbnail = ImageProcessor.scale(it)

                var bitmapFileName = BitmapUtils.getRandomBitmapName()
                var thumbnailFileName = BitmapUtils.getRandomBitmapName()

                val filePath = BitmapUtils.saveBitmapToStorage(applicationContext, it, bitmapFileName)
                val thumbnailPath = BitmapUtils.saveBitmapToStorage(applicationContext, thumbnail, thumbnailFileName)
                if(filePath == null || thumbnail == null) {
                    null
                }else {
                    val timestamp = System.currentTimeMillis()
                    BitmapProcessingResult(filePath!!, thumbnailPath!!, timestamp)
                }
            }
            val okResults = results.filterNotNull()
            withContext(Dispatchers.Main) {
                pictureService.saveBulk(deviceId, okResults) {
                    pictureService.findAll {
                        viewModel.updatePictures(it)
                        runOnUiThread {
                            dialog.dismiss()
                        }
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

    override fun onRemovePicture(picture: Picture) {
        pictureService.remove(picture) {
            loadPictures()
        }
    }

    private fun loadPictures() {
        pictureService.findAll {
                pictures -> viewModel.updatePictures(pictures)
        }
    }

    private fun sync() {
        val backendPictureServices = BackendPictureServices()
        pictureService.findProcessedNonSync { pictures -> run {
            if (pictures.isEmpty()) {
                Toast.makeText(this@PicturesActivity, "No hay muestras por sincronizar", Toast.LENGTH_SHORT).show()
                return@findProcessedNonSync
            }
            val picture = pictures[0]
            UseCaseSyncPicture(
                pictureService,
                boxDetectionServices,
                backendPictureServices
            ).run(picture, object: RequestListener<String> {
                override fun onFailure() {
                    this@PicturesActivity.runOnUiThread {
                        Toast.makeText(this@PicturesActivity, "Error al subir muestra", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onComplete(result: String) {
                    this@PicturesActivity.runOnUiThread {
                        sync()
                        loadPictures()
                        Toast.makeText(this@PicturesActivity, "Se cargó una muestras", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }}
    }

    companion object {
        private val TAG = PicturesActivity::class.java.simpleName
    }
}