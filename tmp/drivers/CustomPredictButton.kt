package com.odrigo.recognitionappkt.drivers

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.odrigo.recognitionappkt.R

class CustomPredictButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val icon: ImageView
    private val text: TextView

    init {
        inflate(context, R.layout.custom_predict_button, this)
        icon = findViewById(R.id.icon)
        text = findViewById(R.id.text)

        // Handle click events or any other customization here
        // For example:
        setOnClickListener {
            // Handle click event
        }
    }

    // You can define methods to set icon, text, etc.
    fun setIcon(iconResId: Int) {
        icon.setImageResource(iconResId)
    }

    fun setText(buttonText: String) {
        text.text = buttonText
    }
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (enabled) {
            setBackgroundResource(R.drawable.custom_predict_button_bg)
            text.setTextColor(resources.getColor(R.color.white))
        } else {
            setBackgroundResource(R.drawable.custom_predict_button_bg)
            text.setTextColor(resources.getColor(R.color.gray300))
        }
    }
}