package com.iiap.deeplarva.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class VersionUtils {
    companion object {
        fun getAppVersion(context: Context): String {
            return try {
                val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    context.packageManager.getPackageInfo(context.packageName, 0)
                }

                val versionName = packageInfo.versionName ?: ""
//                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                    packageInfo.longVersionCode
//                } else {
//                    packageInfo.versionCode.toLong()
//                }

//                "Version Name: $versionName, Version Code: $versionCode"
                versionName
            } catch (e: PackageManager.NameNotFoundException) {
                ""
            }
        }
    }
}