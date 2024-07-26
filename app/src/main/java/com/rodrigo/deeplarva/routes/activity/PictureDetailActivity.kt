package com.rodrigo.deeplarva.routes.activity

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.databinding.ActivityPictureDetailBinding
import com.rodrigo.deeplarva.infraestructure.internal.driver.AppDatabase
import com.rodrigo.deeplarva.infraestructure.internal.driver.DbBuilder
import com.rodrigo.deeplarva.infraestructure.services.BoxDetectionServices
import com.rodrigo.deeplarva.infraestructure.services.PicturesServices
import com.rodrigo.deeplarva.routes.activity.view.PictureDetailActivityView
import kotlin.properties.Delegates

class PictureDetailActivity: AppCompatActivity() {
    private var pictureId by Delegates.notNull<Long>()

    private lateinit var db: AppDatabase
    private lateinit var pictureService: PicturesServices
    private lateinit var binding: ActivityPictureDetailBinding
    private lateinit var view: PictureDetailActivityView
    private lateinit var boxDetectionServices: BoxDetectionServices
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPictureDetailBinding.inflate(layoutInflater)
        view = PictureDetailActivityView(this, binding)

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
                view.render(picture, boxes)
            }}
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}