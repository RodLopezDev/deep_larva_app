package com.rodrigo.deeplarva.modules.camerav2

import androidx.camera.view.PreviewView

interface ICameraV2ProListener {
    fun getFolderName(): String
    fun getPictureFileName(): String
    fun getPreviewView(): PreviewView
    fun onPictureReceived(internalUri: String, contentUri: String)
    fun onErrorPicture()
}