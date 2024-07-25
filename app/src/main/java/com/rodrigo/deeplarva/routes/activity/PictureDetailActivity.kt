package com.rodrigo.deeplarva.routes.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.databinding.ActivityPictureDetailBinding
import com.rodrigo.deeplarva.domain.entity.BoxDetection
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.infraestructure.internal.driver.AppDatabase
import com.rodrigo.deeplarva.infraestructure.internal.driver.DbBuilder
import com.rodrigo.deeplarva.infraestructure.services.BoxDetectionServices
import com.rodrigo.deeplarva.infraestructure.services.PicturesServices
import com.rodrigo.deeplarva.utils.BitmapUtils
import kotlin.properties.Delegates

class PictureDetailActivity: AppCompatActivity() {
    private var pictureId by Delegates.notNull<Long>()

    private lateinit var db: AppDatabase
    private lateinit var pictureService: PicturesServices
    private lateinit var binding: ActivityPictureDetailBinding
    private lateinit var boxDetectionServices: BoxDetectionServices
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPictureDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pictureId = intent.getLongExtra(Constants.INTENT_PICTURE_DETAIL, 0)

        db = DbBuilder.getInstance(this)
        pictureService = PicturesServices(db)
        boxDetectionServices = BoxDetectionServices(db)

        pictureService.findOne(pictureId) { picture ->
            if(picture == null) {
                finish()
                Toast.makeText(this@PictureDetailActivity, "PictureID not found", Toast.LENGTH_SHORT).show()
                return@findOne
            }
            boxDetectionServices.findByPictureId(pictureId) {
                boxes -> run {
                initDetail(picture, boxes)
            }}
        }
    }
    private fun initDetail(picture: Picture,  boxes: List<BoxDetection>) {
        val bitmapFile = BitmapUtils.getBitmapFromPath(picture.filePath)
        runOnUiThread {
            binding.imgBasePicture.setImageBitmap(bitmapFile)
        }
        if(picture.hasMetadata) {
            val bitmapProcessed = BitmapUtils.getBitmapFromPath(picture.processedFilePath)
            runOnUiThread {
                binding.imgProcessedPicture.setImageBitmap(bitmapProcessed)
            }
        }
    }
}