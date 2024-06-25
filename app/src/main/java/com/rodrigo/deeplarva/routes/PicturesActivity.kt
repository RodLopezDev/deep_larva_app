package com.rodrigo.deeplarva.routes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.rodrigo.deeplarva.databinding.ActivityPicturesBinding
import com.rodrigo.deeplarva.domain.Constants
import com.rodrigo.deeplarva.domain.utils.BitmapProcessingResult
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
import java.util.UUID

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

    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        val fileName: String?
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        fileName = cursor?.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        cursor?.close()
        return fileName
    }

    val fileNameMap = mapOf(
        //Piero
        "1000205180.jpg" to "20240502_102606.jpg",
        "1000205181.jpg" to "20240502_103253.jpg",
        "1000205182.jpg" to "20240502_105719.jpg",
        "1000205183.jpg" to "20240502_110948.jpg",
        "1000205184.jpg" to "20240502_112703.jpg",
        "1000205185.jpg" to "20240502_093107_v2.jpg",
        "1000205186.jpg" to "20240502_094150.jpg",
        "1000205187.jpg" to "20240502_100008.jpg",
        "1000205188.jpg" to "20240502_100504.jpg",
        //Jr
        "1000205488.jpg" to "IMG_20240502_101905.jpg",
        "1000205494.jpg" to "IMG_20240502_102244.jpg",
        "1000205495.jpg" to "IMG_20240502_105746.jpg",
        "1000205496.jpg" to "IMG_20240502_111034.jpg",
        "1000205497.jpg" to "IMG_20240502_111421.jpg",
        "1000205498.jpg" to "IMG_20240502_094223.jpg",
        "1000205499.jpg" to "IMG_20240502_094413.jpg",
        "1000205500.jpg" to "IMG_20240502_094827.jpg",
        "1000205501.jpg" to "IMG_20240502_095939.jpg",
        "1000205502.jpg" to "IMG_20240502_101009.jpg",
        //Ing. Rodolfo
        "1000205655.jpg" to "IMG_20240502_100426.jpg",
        "1000205656.jpg" to "IMG_20240502_101850.jpg",
        "1000205657.jpg" to "IMG_20240502_102139.jpg",
        "1000205658.jpg" to "IMG_20240502_102531.jpg",
        "1000205659.jpg" to "IMG_20240502_110929.jpg",
        "1000205660.jpg" to "IMG_20240502_112953.jpg",
        "1000205661.jpg" to "IMG_20240502_113300.jpg",
        "1000205662.jpg" to "IMG_20240502_094845.jpg",
    )


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        view.getDialog().hide()

        if(data == null || resultCode == 0) return
        val bitmaps = view.getDialog().resolve(requestCode, resultCode, data)
        val uri = data.data

        if (uri == null) {
            Toast.makeText(this, "No se pudo obtener el URI de la imagen", Toast.LENGTH_SHORT).show()
            return
        }

        val originalFileName = getFileNameFromUri(applicationContext, uri)

        if (originalFileName == null) {
            Toast.makeText(this, "No se pudo obtener el nombre del archivo", Toast.LENGTH_SHORT).show()
            return
        }

        // Reemplazar el nombre del archivo si está en el mapa
        val newFileName = fileNameMap[originalFileName] ?: originalFileName

        println(originalFileName)
        GlobalScope.launch {
            val results = bitmaps.map {
                var thumbnail = ImageProcessor.scale(it)

                // Obtener el nombre del archivo original
                //val originalFileName = getFileName(applicationContext, uri)

                var bitmapFileName = newFileName

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
                pictureService.saveBulk(okResults) {
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