package com.example.terrariumcontrol111.ui

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    context: Context,
    private val timestamps: List<Long>
) : MarkerView(context, android.R.layout.simple_list_item_1) {

    private val textView: TextView =
        findViewById(android.R.id.text1)

    init {

        // колір тексту
        textView.setTextColor(Color.WHITE)

        // фон marker
        textView.setBackgroundColor(Color.parseColor("#CC1E1E1E"))

        // відступи
        textView.setPadding(20, 10, 20, 10)

        // розмір тексту
        textView.textSize = 12f
    }

    override fun refreshContent(
        e: Entry?,
        highlight: Highlight?
    ) {

        if (e == null) return

        val index = e.x.toInt()

        val time =
            if (index in timestamps.indices) {

                SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(Date(timestamps[index] * 1000))

            } else {
                "--:--"
            }

        val value = String.format("%.1f", e.y)

        textView.text =
            "$value°C\n$time"

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {

        return MPPointF(
            -(width / 2f),
            -height.toFloat() - 20f
        )
    }
}