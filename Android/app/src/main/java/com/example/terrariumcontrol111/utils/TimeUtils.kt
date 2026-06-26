package com.example.terrariumcontrol111.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtils {

    fun formatTimestamp(timestamp: Long): String {

        return try {

            val time = timestamp * 1000L

            val sdf = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())

            sdf.format(Date(time))

        } catch (e: Exception) {

            "Unknown time"
        }
    }
}