package com.example.terrariumcontrol111.model

data class SensorData(

    var tempSoil: Float? = 0f,
    var soilMoisture: Float? = 0f,
    var tempAir: Float? = 0f,
    var timestamp: Long? = 0
)