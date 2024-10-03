package com.deeplarva.iiap.gob.pe.routes.activity.about_us

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.deeplarva.iiap.gob.pe.databinding.ActivityAboutusBinding

class AboutUsActivity: AppCompatActivity()  {
    private lateinit var binding: ActivityAboutusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutusBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}