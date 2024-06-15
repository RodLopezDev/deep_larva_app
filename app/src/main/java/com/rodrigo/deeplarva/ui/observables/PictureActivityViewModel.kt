package com.rodrigo.deeplarva.ui.observables

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rodrigo.deeplarva.domain.view.PictureListEntity

class PictureActivityViewModel : ViewModel() {
    private val _pictures = MutableLiveData<List<PictureListEntity>>().apply {
        value = mutableListOf<PictureListEntity>()
    }

    val pictures: LiveData<List<PictureListEntity>> = _pictures

    fun updatePictures(pictures: List<PictureListEntity>) {
        _pictures.value = pictures
    }
}