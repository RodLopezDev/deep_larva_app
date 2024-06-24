package com.rodrigo.deeplarva.modules.camera

import androidx.camera.view.PreviewView

interface ICameraProListener {
    fun getFolderName(): String
    fun getPictureFileName(): String
    fun getPreviewView(): PreviewView
    fun onPictureReceived(picturePath: String)
    fun onErrorPicture()
}