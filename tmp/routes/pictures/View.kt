package com.odrigo.recognitionappkt.routes.pictures

import ItemSpacingDecoration
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.odrigo.recognitionappkt.R
import com.odrigo.recognitionappkt.adapters.PictureAdapterView
import com.odrigo.recognitionappkt.domain.Picture
import com.odrigo.recognitionappkt.drivers.CustomPredictButton
import com.odrigo.recognitionappkt.drivers.RecyclerItemClickListener
import com.odrigo.recognitionappkt.drivers.photo.PhotoByCameraHandler
import com.odrigo.recognitionappkt.drivers.photo.PhotoByStorageHandler
import com.odrigo.recognitionappkt.drivers.photo.PhotoFactory
import com.odrigo.recognitionappkt.routes.pictures.facades.ViewFacade

class View(activity: AppCompatActivity, facade: ViewFacade) {

    private var my: AppCompatActivity
    private var rvPictures: RecyclerView
    private var btnHidePanel: Button
    private var btnLoadPicture: Button
    private var btnCamera: Button
    private var btnNewPicture: FloatingActionButton
    private var btnPredict: CustomPredictButton
    private var rlPanelNewPicture: RelativeLayout

    private var gridLayoutManager: GridLayoutManager
    private val photoFactory = PhotoFactory()

    init {
        my = activity
        activity.setContentView(R.layout.activity_subsample)

        rvPictures = activity.findViewById(R.id.rv_pictures)
        btnNewPicture = activity.findViewById(R.id.btn_new_picture)
        btnPredict = activity.findViewById(R.id.btn_predict)
        btnLoadPicture = activity.findViewById(R.id.btn_load_pic)
        btnCamera = activity.findViewById(R.id.btn_camera)
        rlPanelNewPicture = activity.findViewById(R.id.rl_panel_new_picture)
        btnHidePanel = activity.findViewById(R.id.btn_hide_panel)

        val spacingInPixels = activity.resources.getDimensionPixelSize(R.dimen.spacing)
        rvPictures.addItemDecoration(ItemSpacingDecoration(spacingInPixels))

        val byCamera = PhotoByCameraHandler(activity)
        val byStorage = PhotoByStorageHandler(activity)
        photoFactory.add(byCamera)
        photoFactory.add(byStorage)

        btnNewPicture.setOnClickListener {
            showPanel()
        }

        btnHidePanel.setOnClickListener {
            hidePanel()
        }

        btnLoadPicture.setOnClickListener {
            byStorage.launch()
        }
        btnCamera.setOnClickListener {
            byCamera.launch()
        }

        btnPredict.setOnClickListener {
            facade.eventPredict()
        }

        gridLayoutManager = GridLayoutManager(activity, 2)

        rvPictures.addOnItemTouchListener(
            RecyclerItemClickListener(
                activity,
                rvPictures,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        facade.addDeletable(position)
                        Toast.makeText(activity, "onItemClick $position", Toast.LENGTH_SHORT).show()
                    }
                    override fun onItemLongClick(view: View, position: Int) {
                        facade.enableDeletion(position)
                        Toast.makeText(activity, "onItemLongClick $position", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        )
    }

    fun loadPictures(pictures: List<Picture>) {
        val adapter = PictureAdapterView(pictures)
        rvPictures.adapter = adapter
        rvPictures.layoutManager = gridLayoutManager
    }

    fun hidePanel(){
        my.supportActionBar?.show()
        btnNewPicture.visibility = View.VISIBLE
        rlPanelNewPicture.visibility = View.INVISIBLE
    }

    private fun showPanel(){
        my.supportActionBar?.hide()
        btnNewPicture.visibility = View.INVISIBLE
        rlPanelNewPicture.visibility = View.VISIBLE
    }

    fun getPhotoFactory(): PhotoFactory {
        return photoFactory
    }

    fun getBtnPredict(): CustomPredictButton {
        return btnPredict
    }
}