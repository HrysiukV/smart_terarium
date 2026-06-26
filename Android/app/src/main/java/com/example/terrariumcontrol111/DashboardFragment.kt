package com.example.terrariumcontrol111

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val tvAirTemp = view.findViewById<TextView>(R.id.tvAirTemp)
        val tvPressure = view.findViewById<TextView>(R.id.tvPressure)
        val tvSoilTemp = view.findViewById<TextView>(R.id.tvSoilTemp)
        val tvSoilMoisture = view.findViewById<TextView>(R.id.tvSoilMoisture)
        val tvCurrentMode = view.findViewById<TextView>(R.id.tvCurrentMode)
        val tvDayNight = view.findViewById<TextView>(R.id.tvDayNight)

        view.findViewById<Button>(R.id.btnManual).setOnClickListener { setMode("manual") }
        view.findViewById<Button>(R.id.btnDay).setOnClickListener { setMode("day") }
        view.findViewById<Button>(R.id.btnNight).setOnClickListener { setMode("night") }

        val terrariumRef = FirebaseDatabase.getInstance().getReference("terrarium")

        terrariumRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val airTemp = snapshot.child("tempAir").getValue(Double::class.java) ?: 0.0
                    val pressure = snapshot.child("pressure").getValue(Double::class.java) ?: 0.0
                    val soilTemp = snapshot.child("tempSoil").getValue(Double::class.java) ?: 0.0
                    val moisture = snapshot.child("soilMoisture").getValue(Int::class.java) ?: 0
                    val mode = snapshot.child("currentMode").getValue(String::class.java) ?: "manual"
                    val isDay = snapshot.child("isDay").getValue(Boolean::class.java) ?: true

                    tvAirTemp.text = "Температура: %.1f °C".format(airTemp)
                    tvPressure.text = "Тиск: %.1f мм рт. ст.".format(pressure)
                    tvSoilTemp.text = "Температура: %.1f °C".format(soilTemp)
                    tvSoilMoisture.text = "Вологість: $moisture %"

                    tvCurrentMode.text = "Режим: ${mode.uppercase()}"
                    tvDayNight.text = if (isDay) "☀️ ДЕНЬ" else "🌙 НІЧ"
                } catch (e: Exception) {}
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return view
    }

    private fun setMode(mode: String) {
        FirebaseDatabase.getInstance().getReference("terrarium/mode").setValue(mode)
    }
}