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
import com.rodrigo.deeplarva.UseCaseSyncPicture
import com.rodrigo.deeplarva.databinding.ActivityPicturesBinding
import com.rodrigo.deeplarva.domain.Constants
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.domain.utils.BitmapProcessingResult
import com.rodrigo.deeplarva.infraestructure.DbBuilder
import com.rodrigo.deeplarva.infraestructure.driver.AppDatabase
import com.rodrigo.deeplarva.modules.requests.RequestListener
import com.rodrigo.deeplarva.modules.requests.RequestManager
import com.rodrigo.deeplarva.routes.observables.PictureActivityViewModel
import com.rodrigo.deeplarva.routes.services.BoxDetectionServices
import com.rodrigo.deeplarva.routes.services.PicturesServices
import com.rodrigo.deeplarva.routes.view.IPictureViewListener
import com.rodrigo.deeplarva.routes.view.PictureActivityView
import com.rodrigo.deeplarva.routes.view.PictureViewListener
import com.rodrigo.deeplarva.utils.BitmapUtils
import com.rodrigo.deeplarva.utils.ImageProcessor
import com.rodrigo.deeplarva.utils.PreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

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
        view.getDialog().hide()

        if(data == null || resultCode == 0) return
        val bitmaps = view.getDialog().resolve(requestCode, resultCode, data)

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
        pictureService.findProcessed { pictures -> run {
            if (pictures.isEmpty()) {
                return@findProcessed
            }
            val picture = pictures[0]
            UseCaseSyncPicture(boxDetectionServices).run(picture, object: RequestListener {
                override fun onFailure() {
                    this@PicturesActivity.runOnUiThread {
                        Toast.makeText(this@PicturesActivity, "ERROR", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onComplete() {
                    this@PicturesActivity.runOnUiThread {
                        Toast.makeText(this@PicturesActivity, "COMPLETED", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }}
    }

    companion object {
        private val TAG = PicturesActivity::class.java.simpleName
    }
}