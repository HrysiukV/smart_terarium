package com.example.terrariumcontrol111

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(
    private val context: Context
) {

    private val channelId =
        "terrarium_alerts"

    init {
        createChannel()
    }

    private fun createChannel() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    channelId,
                    "Terrarium Alerts",
                    NotificationManager
                        .IMPORTANCE_HIGH
                )

            val manager =
                context.getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(
                channel
            )
        }
    }

    fun showNotification(
        title: String,
        message: String
    ) {

        val builder =
            NotificationCompat.Builder(
                context,
                channelId
            )
                .setSmallIcon(
                    android.R.drawable
                        .ic_dialog_alert
                )
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(
                    NotificationCompat
                        .PRIORITY_HIGH
                )

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        manager.notify(
            System.currentTimeMillis()
                .toInt(),
            builder.build()
        )
    }
}