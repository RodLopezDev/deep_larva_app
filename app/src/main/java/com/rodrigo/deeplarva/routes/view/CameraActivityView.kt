package com.rodrigo.deeplarva.routes.view

import android.util.Log
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.databinding.ActivityCameraBinding
import com.rodrigo.deeplarva.routes.observables.CameraParamsViewModel

class CameraActivityView(
    private val activity: AppCompatActivity,
    private val viewModel: CameraParamsViewModel
) {
    private var swtControls: Switch = activity.findViewById(R.id.swtControls)

    private var sbExposure: SeekBar = activity.findViewById(R.id.sbExposure)
    private var sbISO: SeekBar = activity.findViewById(R.id.sbISO)
    private var sbSpeed: SeekBar = activity.findViewById(R.id.sbSpeed)

    private var tvExposure: TextView = activity.findViewById(R.id.tvExposure)
    private var tvISO: TextView = activity.findViewById(R.id.tvISO)
    private var tvSpeed: TextView = activity.findViewById(R.id.tvSpeed)

    init {
        swtControls.setOnCheckedChangeListener { _, isChecked ->
            setControlVisibility(isChecked)
        }

        sbExposure.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.updateEV(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbISO.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.updateISO(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.updateSpeed(progress.toLong())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        setControlVisibility(swtControls.isChecked)

//        btnCapture.setOnClickListener {
//        }
    }

    fun setEvText(ev: Int) {
        tvExposure.text = "Exposure: $ev"
    }

    fun setISOText(iso: Int) {
        tvISO.text = "ISO: $iso"
    }

    fun setSpeedText(speed: Long) {
        tvSpeed.text = "Speed: $speed"
    }

    private fun setControlVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) SeekBar.VISIBLE else SeekBar.GONE
        sbExposure.visibility = visibility
        tvExposure.visibility = visibility

        sbISO.visibility = visibility
        tvISO.visibility = visibility

        sbSpeed.visibility = visibility
        tvSpeed.visibility = visibility
    }
}