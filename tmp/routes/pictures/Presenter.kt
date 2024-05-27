package com.odrigo.recognitionappkt.routes.pictures

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.odrigo.recognitionappkt.R
import com.odrigo.recognitionappkt.domain.Picture
import com.odrigo.recognitionappkt.domain.SubSample
import com.odrigo.recognitionappkt.drivers.BitmapManager
import com.odrigo.recognitionappkt.routes.pictures.components.ProgressViewComponent
import com.odrigo.recognitionappkt.routes.pictures.components.ResultsViewComponent
import com.odrigo.recognitionappkt.routes.pictures.facades.ViewFacade

class Presenter : AppCompatActivity(), ViewFacade {

    private lateinit var view: View
    private lateinit var state: State
    private lateinit var service: Service

    private lateinit var resultComponent: ResultsViewComponent
    private lateinit var progressComponent: ProgressViewComponent


    private var backgroundTask = BackgroundTaskPredict(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subsample)

        view = View(this, this)
        state = State(this)
        service = Service(this)

        resultComponent = ResultsViewComponent(this)
        progressComponent = ProgressViewComponent(this)

        val actionBar = supportActionBar
        actionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Submuestra: ${state.subSampleId}"
        }

        service.getSubSampleCOROUTINE(state.subSampleId) {
            subSample -> run {
            if (subSample == null) {
                Toast.makeText(this, "SubSample Not Found", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                state.subSample = subSample
                resultComponent.setResult(state.subSample)
                service.getPicturesCOROUTINE(state.subSampleId) {
                    pictures ->  view.loadPictures(pictures)
                }
            }
        }}
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_subsample, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 0){
            return
        }

        var bitmap = view.getPhotoFactory().resolve(requestCode, resultCode, data)
        view.hidePanel()

        val filename = BitmapManager.getRandomBitmapName()
        val filePath = BitmapManager.saveBitmapToStorage(this, bitmap, filename)
        if (filePath == null) {
            Toast.makeText(this, "ERROR AL CARGAR IMAGEN", Toast.LENGTH_LONG).show()
            return
        }

        service.createPictureCOROUTINE(filePath, state.subSampleId) {
            service.getPicturesCOROUTINE(state.subSampleId) {
                pictures ->  view.loadPictures(pictures)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
//            R.id.menu_btn_run -> {
//                eventPredictBatch()
//                true
//            }
//            R.id.menu_btn_save -> {
//                true
//            }
//            R.id.menu_btn_options -> {
//                true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun eventUpdatePredictionProgress(status: Int) {
        progressComponent.updateProgress(status)
    }

    private fun eventEntityPredictionProgress(id: Long, counter: Int, bitmapProcessedPath: String, callback: () -> Unit) {
        service.getPictureCOROUTINE(id) {
                picture -> run {
                if (picture != null) {
                    service.updatePictureCOROUTINE(
                        Picture(
                            id = picture.id,
                            count = counter,
                            filePath = picture.filePath,
                            subSampleId = picture.subSampleId,
                            hasMetadata = true,
                            processedFilePath = bitmapProcessedPath
                        )
                    ) {
                        callback()
                    }
                }
            }
        }
    }

    private fun eventFinishPrediction() {
        Toast.makeText(this, "FINISHED PREDICTION", Toast.LENGTH_LONG).show()
        view.getBtnPredict().isEnabled = false
        title = "Imágenes - ${state.subSampleId}"
        updateSubSampleInfo()
        progressComponent.hide()
        service.getPicturesCOROUTINE(state.subSampleId) {
                pictures ->  view.loadPictures(pictures)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun eventPredict() {
        if(backgroundTask.isProcessing){
            Toast.makeText(this, "Process is running yet", Toast.LENGTH_LONG).show()
            return
        }
        service.getUnProcessedPicturesCOROUTINE(state.subSampleId) {
                pictures ->
            if (pictures.isNotEmpty()){
                progressComponent.show()
                Toast.makeText(this, "PROCESSING, ${pictures.size}", Toast.LENGTH_LONG).show()
                backgroundTask.predictBatchCOROUTINE(
                    pictures,
                    ::eventUpdatePredictionProgress,
                    ::eventEntityPredictionProgress,
                    ::eventFinishPrediction
                )
            } else {
                updateSubSampleInfo()
                Toast.makeText(this, "There arent pictures tu process", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateSubSampleInfo() {
        service.getProcessedPicturesCOROUTINE(state.subSampleId) {
            pictures -> run {
            if(pictures.isNotEmpty()){
                val min = pictures.minOf { it.count }
                val max = pictures.maxOf { it.count }
                val valuesList = pictures.map { it.count }.distinct()
                val fashionCounts = valuesList.groupingBy { it }.eachCount()
                val mostCommonFashion = fashionCounts.maxByOrNull { it.value }?.key

                val updated = SubSample(id=state.subSample.id, isTraining = true, min = min, max = max, mean = mostCommonFashion?: 0)
                service.updateSubSampleCOROUTINE(updated) {
                    resultComponent.setResult(updated)
                }
            }
        }}
    }

    override fun enableDeletion(index: Int) {
        //TODO("Not yet implemented")
    }

    override fun addDeletable(index: Int) {
        //TODO("Not yet implemented")
    }
}