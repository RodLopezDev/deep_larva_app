package com.rodrigo.deeplarva.routes.activity.view

import android.graphics.Bitmap
import android.widget.Toast
import com.rodrigo.deeplarva.databinding.ActivityPictureDetailBinding
import com.rodrigo.deeplarva.domain.entity.BoxDetection
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.domain.view.ExportableDataPicture
import com.rodrigo.deeplarva.routes.activity.PictureDetailActivity
import com.rodrigo.deeplarva.ui.widget.progressDialog.ProgressDialog
import com.rodrigo.deeplarva.utils.Base64utils
import com.rodrigo.deeplarva.utils.BitmapUtils
import com.rodrigo.deeplarva.utils.FileUtils
import com.rodrigo.deeplarva.utils.XmlUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PictureDetailActivityView(
    private val activity: PictureDetailActivity,
    private val binding: ActivityPictureDetailBinding,
) {
    init {
        activity.setContentView(binding.root)
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.apply {
            title = "Detalle"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    fun render(picture: Picture, boxes: List<BoxDetection>) {
        val bitmapFile = BitmapUtils.getBitmapFromPath(picture.filePath)
        if(bitmapFile == null) {
            return
        }

        activity.runOnUiThread {
            binding.imgBasePicture.setImageBitmap(bitmapFile)
        }
        var bitmapProcessed: Bitmap? = null
        if(picture.hasMetadata) {
            bitmapProcessed = BitmapUtils.getBitmapFromPath(picture.processedFilePath)
            activity.runOnUiThread {
                binding.imgProcessedPicture.setImageBitmap(bitmapProcessed)
            }
        }

        binding.btnDemo.setOnClickListener {
            val dialog = ProgressDialog()
            dialog.show(activity)
            GlobalScope.launch {
                val bmFileStr = if(bitmapFile != null) Base64utils.bitmapToBase64(bitmapFile) else ""
                val bmProcessedFileStr = if(bitmapProcessed != null) Base64utils.bitmapToBase64(bitmapProcessed) else ""
                val data = ExportableDataPicture(
                    ExportableDataPicture.PictureData.build(picture),
                    ExportableDataPicture.BoxDetectionData.buildList(boxes),
                    bmFileStr,
                    bmProcessedFileStr
                )
                val dataString = XmlUtils.serializeToXml(data)
                val fileName = "DL-${picture.uuid}.xml"
                val file = FileUtils(activity).saveToInternalStorage(fileName, dataString)
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    if(file != null) {
                        FileUtils(activity).shareMultiApp(file)
                    } else {
                        Toast.makeText(activity, "File saved as $fileName", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}