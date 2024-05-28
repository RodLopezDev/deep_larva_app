package com.rodrigo.deeplarva.routes.observables

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.domain.entity.SubSample

class PictureActivityViewModel : ViewModel() {
    private val _subSample = MutableLiveData<SubSample?>().apply {
        value = null
    }
    private val _pictures = MutableLiveData<List<Picture>>().apply {
        value = mutableListOf<Picture>()
    }

    val subSample: LiveData<SubSample?> = _subSample
    val pictures: LiveData<List<Picture>> = _pictures

    fun updateSubSample(subsample: SubSample) {
        _subSample.value = subsample
    }
    fun updatePictures(pictures: List<Picture>) {
        _pictures.value = pictures
    }
}