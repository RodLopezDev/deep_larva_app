package com.iiap.deeplarva.gob.pe.domain.constants

import android.Manifest
import android.os.Build

class PermissionsConstans {
    companion object {
        val REQUIRE_CONTRACT_FOR_GALLERY: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

        fun getPermissionsList (): List<String> {
            // REF: https://stackoverflow.com/questions/75628155/read-external-storage-permission-request-not-showing-on-emulator
            // REF: https://stackoverflow.com/questions/72948052/android-13-read-external-storage-permission-still-usable
            val sdk = Build.VERSION.SDK_INT

            val permissions = mutableListOf<String>(Manifest.permission.INTERNET, Manifest.permission.CAMERA)
            if(sdk <= Build.VERSION_CODES.S){
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            return permissions
        }
    }
}