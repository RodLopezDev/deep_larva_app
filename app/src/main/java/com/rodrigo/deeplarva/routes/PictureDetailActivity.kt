package com.rodrigo.deeplarva.routes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.domain.Constants
import com.rodrigo.deeplarva.infraestructure.DbBuilder
import com.rodrigo.deeplarva.infraestructure.driver.AppDatabase
import com.rodrigo.deeplarva.routes.services.BoxDetectionServices
import com.rodrigo.deeplarva.routes.services.PicturesServices
import kotlin.properties.Delegates

class PictureDetailActivity: AppCompatActivity() {
    private var pictureId by Delegates.notNull<Long>()

    private lateinit var db: AppDatabase
    private lateinit var pictureService: PicturesServices
    private lateinit var boxDetectionServices: BoxDetectionServices
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pictureId = intent.getLongExtra(Constants.INTENT_PICTURE_DETAIL, 0)

        db = DbBuilder.getInstance(this)
        pictureService = PicturesServices(db)
        boxDetectionServices = BoxDetectionServices(db)

        pictureService.findOne(pictureId) {
            boxDetectionServices.findByPictureId(pictureId) {
                boxes -> {

            }}
        }
    }
}