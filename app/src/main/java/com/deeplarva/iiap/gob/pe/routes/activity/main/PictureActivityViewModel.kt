package com.deeplarva.iiap.gob.pe.routes.activity.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.deeplarva.iiap.gob.pe.domain.view.PictureListEntity

class PictureActivityViewModel : ViewModel() {
    private val _pictures = MutableLiveData<List<PictureListEntity>>().apply {
        value = mutableListOf<PictureListEntity>()
    }

    val pictures: LiveData<List<PictureListEntity>> = _pictures

    fun updatePictures(pictures: List<PictureListEntity>) {
        _pictures.value = pictures
    }
}