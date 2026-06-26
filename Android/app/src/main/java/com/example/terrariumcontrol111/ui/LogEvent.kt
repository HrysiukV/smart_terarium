package com.example.terrariumcontrol111.ui

data class LogEvent(
    val action: String = "",
    val device: String = "",
    val reason: String = "",
    val timestamp: Long = 0L,
    val type: String = "device" // "device" або "mode"
)