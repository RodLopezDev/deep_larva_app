package com.iiap.deeplarva.gob.pe.routes.activity.picture_detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.iiap.deeplarva.gob.pe.R
import com.iiap.deeplarva.gob.pe.databinding.ActivityPictureDetailBinding
import com.iiap.deeplarva.gob.pe.domain.constants.AppConstants
import com.iiap.deeplarva.gob.pe.infraestructure.internal.driver.AppDatabase
import com.iiap.deeplarva.gob.pe.infraestructure.internal.driver.DbBuilder
import com.iiap.deeplarva.gob.pe.infraestructure.services.BoxDetectionServices
import com.iiap.deeplarva.gob.pe.infraestructure.services.PicturesServices
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

        pictureId = intent.getLongExtra(AppConstants.INTENT_PICTURE_DETAIL, 0)

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
            R.id.action_export -> {
                view.export()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_picture_detail, menu)
        return true
    }
}