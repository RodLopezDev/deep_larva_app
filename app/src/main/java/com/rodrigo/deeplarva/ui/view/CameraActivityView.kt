package com.rodrigo.deeplarva.ui.view

import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.modules.camera.domain.CameraCharacteristic
import com.rodrigo.deeplarva.modules.camera.interfaces.CameraActivityViewListener

class CameraActivityView(
    private val activity: AppCompatActivity
) {
    private var cameraCharacteristic: CameraCharacteristic? = null

    var textureView: TextureView  = activity.findViewById(R.id.textureView)

    private var llShadowTexture: LinearLayout = activity.findViewById(R.id.llShadowTexture)
    private var llCommandControl: LinearLayout = activity.findViewById(R.id.llCommandControl)
    private var btnClose: Button = activity.findViewById(R.id.btnClose)
    private var btnCapture: Button = activity.findViewById(R.id.btnCapture)
    private var swtControls: Switch = activity.findViewById(R.id.swtControls)

    private var sbISO: SeekBar = activity.findViewById(R.id.sbISO)
    private var sbSpeed: SeekBar = activity.findViewById(R.id.sbSpeed)

    private var tvISO: TextView = activity.findViewById(R.id.tvISO)
    private var tvSpeed: TextView = activity.findViewById(R.id.tvSpeed)

    init {
        setControlVisibility(swtControls.isChecked)
        llCommandControl.visibility = View.INVISIBLE
    }

    fun initializeCommandControl(listener: CameraActivityViewListener, cameraCharacteristic: CameraCharacteristic) {
        this.cameraCharacteristic = cameraCharacteristic

        setISOText(100, cameraCharacteristic!!.isoRange.upper)
        setSpeedText(100, cameraCharacteristic!!.speedRange.upper)

        sbISO.progress = 100
        sbSpeed.progress = 100

        swtControls.setOnCheckedChangeListener { _, isChecked ->
            setControlVisibility(isChecked)
        }

        sbISO.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val converted = getISOConverted(progress)
                setISOText(progress, converted)
                listener.onChangeISO(converted)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val converted = getSpeedConverted(progress)
                setSpeedText(progress, converted)
                listener.onChangeSpeed(converted)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnCapture.setOnClickListener {
            listener.onCapture()
        }

        btnClose.setOnClickListener {
            listener.onCloseView()
        }

        llCommandControl.visibility = View.VISIBLE
        llShadowTexture.visibility = View.INVISIBLE
    }

    fun showTextureView() {
        // TODO: REVIEW THIS
        llShadowTexture.visibility = View.INVISIBLE
    }

    private fun getISOConverted(value: Int): Int {
        val min = cameraCharacteristic!!.isoRange.lower
        val max = cameraCharacteristic!!.isoRange.upper
        return min + (value * (max - min) / 100)
    }

    private fun getSpeedConverted(value: Int): Long {
        val min = cameraCharacteristic!!.speedRange.lower
        val max = cameraCharacteristic!!.speedRange.upper
        return min + (value * (max - min) / 100)
    }

    private fun setISOText(percentage: Int, value: Int) {
        val min = cameraCharacteristic!!.isoRange.lower
        val max = cameraCharacteristic!!.isoRange.upper

        tvISO.text = "[$min-$max] ISO: $value, $percentage%"
    }

    private fun setSpeedText(percentage: Int, value: Long) {
        val min = cameraCharacteristic!!.speedRange.lower
        val max = cameraCharacteristic!!.speedRange.upper
        tvSpeed.text = "[$min-$max] Speed: $value, $percentage%"
    }

    private fun setControlVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) SeekBar.VISIBLE else SeekBar.GONE

        sbISO.visibility = visibility
        tvISO.visibility = visibility

        sbSpeed.visibility = visibility
        tvSpeed.visibility = visibility
    }
}