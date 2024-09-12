package com.iiap.deeplarva.utils

import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.iiap.deeplarva.domain.constants.AppConstants
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class FileUtils(private val activity: AppCompatActivity) {
    fun saveOnStorage(image: Image, path: String, fileName: String): File {
        val imageFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), path)
        if (!imageFolder.exists()) {
            imageFolder.mkdirs()
        }

        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        val file = File(imageFolder, "$fileName${AppConstants.IMAGE_EXTENSION}")
        try {
            val output = FileOutputStream(file)
            output.write(bytes)
            output.close()
            return file
        } catch (e: IOException) {
            throw e
        }
    }
    fun saveToInternalStorage(fileName: String, fileContent: String): File? {
        return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            try {
                val file = File(activity.getExternalFilesDir(null), fileName)
                val fileOutputStream = FileOutputStream(file)
                fileOutputStream.write(fileContent.toByteArray())
                fileOutputStream.close()
                return file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }
    fun saveBitmapToExternalStorage(bitmap: Bitmap, path: String, filename: String): File {
        val imageFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), path)
        if (!imageFolder.exists()) {
            imageFolder.mkdirs()
        }

        val file = File(imageFolder, "$filename${AppConstants.IMAGE_EXTENSION}")

        try {
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream?.flush()
            fileOutputStream?.close()
            return file
        } catch (e: IOException) {
            throw e
        }
    }

    private fun getUriForFile(file: File): Uri {
        return FileProvider.getUriForFile(activity, AppConstants.FILE_PROVIDER, file)
    }
    fun shareFile(file: File, packageName: String? = null) {
        val uri = getUriForFile(file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            if (packageName != null) {
                setPackage(packageName)
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(Intent.createChooser(intent, "Share File"))
        } else {
            activity.run {
                Toast.makeText(activity, "No app found to share the file", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun shareMultiApp(file: File) {
        val uri = getUriForFile(file)

        // Create intents for each specific app
        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val gmailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.google.android.gm")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val driveIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.google.android.apps.docs")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }


        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Subject Here")  // Optional: Set a subject
            putExtra(Intent.EXTRA_TEXT, "Body Here")  // Optional: Set a body
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Check if the apps are installed and create a chooser intent
        val intentChooser = Intent.createChooser(gmailIntent, "Share File")
        val intentList = mutableListOf<Intent>()

        if (whatsappIntent.resolveActivity(activity.packageManager) != null) {
            intentList.add(whatsappIntent)
        }
        if (gmailIntent.resolveActivity(activity.packageManager) != null) {
            intentList.add(gmailIntent)
        }
        if (driveIntent.resolveActivity(activity.packageManager) != null) {
            intentList.add(driveIntent)
        }
        if (emailIntent.resolveActivity(activity.packageManager) != null) {
            intentList.add(emailIntent)
        }

        if (intentList.isNotEmpty()) {
            intentChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toTypedArray())
            activity.startActivity(intentChooser)
        } else {
            Toast.makeText(activity, "No suitable app found to share the file", Toast.LENGTH_SHORT).show()
        }
    }
}