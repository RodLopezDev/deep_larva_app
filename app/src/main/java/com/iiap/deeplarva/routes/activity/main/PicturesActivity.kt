package com.iiap.deeplarva.routes.activity.main

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.iiap.deeplarva.R
import com.iiap.deeplarva.application.usecases.UseCaseLoadPictures
import com.iiap.deeplarva.application.usecases.UseCaseLoadPicturesProcessesRunning
import com.iiap.deeplarva.application.usecases.UseCaseSyncPicture
import com.iiap.deeplarva.databinding.ActivityPicturesBinding
import com.iiap.deeplarva.domain.constants.CloudKeysConstants
import com.iiap.deeplarva.domain.constants.SharedPreferencesConstants
import com.iiap.deeplarva.domain.entity.Picture
import com.iiap.deeplarva.domain.view.BitmapProcessingResult
import com.iiap.deeplarva.modules.inputHelper.PictureByCameraProHandler
import com.iiap.deeplarva.modules.inputHelper.PictureByStorageHandler
import com.iiap.deeplarva.infraestructure.internal.driver.AppDatabase
import com.iiap.deeplarva.infraestructure.internal.driver.DbBuilder
import com.iiap.deeplarva.infraestructure.services.BackendPictureServices
import com.iiap.deeplarva.infraestructure.services.BoxDetectionServices
import com.iiap.deeplarva.infraestructure.services.PicturesServices
import com.iiap.deeplarva.modules.requests.RequestListener
import com.iiap.deeplarva.routes.activity.configuration.ConfigurationsActivity
import com.iiap.deeplarva.routes.service.binder.IBoundService
import com.iiap.deeplarva.ui.adapter.PictureItemListListener
import com.iiap.deeplarva.ui.widget.progressDialog.ProgressDialog
import com.iiap.deeplarva.utils.BitmapUtils
import com.iiap.deeplarva.utils.PreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PicturesActivity: BoundedActivity(), IPictureViewListener, IBoundService {
    private lateinit var view: PictureActivityView
    private lateinit var binding: ActivityPicturesBinding

    private lateinit var db: AppDatabase
    private lateinit var pictureService: PicturesServices
    private lateinit var boxDetectionServices: BoxDetectionServices
    private lateinit var viewModel: PictureActivityViewModel

    private lateinit var deviceId: String

    val photoPickerLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            val bitmaps = BitmapUtils.getBitmapFromUri(applicationContext, uri)
            val dialog = ProgressDialog()
            dialog.show(this@PicturesActivity)
            processBitmaps(bitmaps) {
                dialog.dismiss()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPicturesBinding.inflate(layoutInflater)

        db = DbBuilder.getInstance(this)
        pictureService = PicturesServices(db)
        boxDetectionServices = BoxDetectionServices(db)
        deviceId = PreferencesHelper(this).getString(SharedPreferencesConstants.DEVICE_ID)!!

        view = PictureActivityView(deviceId, this, binding, this)
        viewModel = ViewModelProvider(this)[PictureActivityViewModel::class.java]

        viewModel.pictures.observe(this) {
            view.loadPictures(it, object: PictureItemListListener {
                override fun onPredict(picture: Picture) {
                    launchService(picture)
                }
            })
        }
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
            R.id.action_config -> {
                goToConfig()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBindToService() {
        super.onBindToService()
        load()
    }

    private fun load() {
        if(this.hasPictureId() != null) {
            UseCaseLoadPicturesProcessesRunning(pictureService)
                .execute(this.hasPictureId()!!) {pictures -> run {
                    viewModel.updatePictures(pictures)
                }}
            return
        }
        UseCaseLoadPictures(pictureService).execute {
            viewModel.updatePictures(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data == null || resultCode == 0) return
        if(listOf(PictureByStorageHandler.REQUESTCODE, PictureByCameraProHandler.REQUESTCODE).indexOf(requestCode) == -1) {
            return
        }

        val dialog = ProgressDialog()
        dialog.show(this@PicturesActivity)

        val bitmaps = view.resolve(requestCode, resultCode, data)
        processBitmaps(bitmaps) {
            dialog.dismiss()
        }
    }

    private fun processBitmaps(bitmaps: List<Bitmap>, callback: () -> Unit){
        GlobalScope.launch {
            val results = bitmaps.map {
                var thumbnail = BitmapUtils.scale(it)

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
                    load()
                    runOnUiThread {
                        callback()
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
        load()
    }

    override fun onStartService(pictureId: Long) {
        super.onStartService(pictureId)
        load()
    }

    override fun onRemovePicture(picture: Picture) {
        pictureService.remove(picture) {
            load()
        }
    }

    private fun goToConfig() {
        val intent = Intent(this, ConfigurationsActivity::class.java)
        startActivity(intent)
    }

    private fun sync() {
        val helper = PreferencesHelper(this)
        val serverUrl = helper.getString(CloudKeysConstants.SERVER_URL, "") ?: ""
        val serverApiKey = helper.getString(CloudKeysConstants.SERVER_API_KEY, "") ?: ""
        if(serverApiKey == "" || serverUrl == "") {
            Toast.makeText(this, "Error al obtener configuración de servidor", Toast.LENGTH_SHORT).show()
            return
        }

        val backendPictureServices = BackendPictureServices(serverUrl!!, serverApiKey!!)
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
                        load()
                        Toast.makeText(this@PicturesActivity, "Se cargó una muestras", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }}
    }
}