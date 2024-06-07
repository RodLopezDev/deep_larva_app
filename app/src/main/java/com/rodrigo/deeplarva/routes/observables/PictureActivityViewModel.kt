package com.rodrigo.deeplarva.routes.observables

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rodrigo.deeplarva.domain.entity.Picture

class PictureActivityViewModel : ViewModel() {
    private val _pictures = MutableLiveData<List<Picture>>().apply {
        value = mutableListOf<Picture>()
    }

    val pictures: LiveData<List<Picture>> = _pictures

    fun updatePictures(pictures: List<Picture>) {
        _pictures.value = pictures
    }
}