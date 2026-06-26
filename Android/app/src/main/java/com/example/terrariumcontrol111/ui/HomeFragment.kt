package com.example.terrariumcontrol111.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.terrariumcontrol111.R
import com.google.firebase.database.*

import java.util.Calendar

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvAirTemp = view.findViewById<TextView>(R.id.tvAirTemp)
        val tvPressure = view.findViewById<TextView>(R.id.tvPressure)
        val tvSoilTemp = view.findViewById<TextView>(R.id.tvSoilTemp)
        val tvSoilMoisture = view.findViewById<TextView>(R.id.tvSoilMoisture)
        val tvCurrentMode = view.findViewById<TextView>(R.id.tvCurrentMode)
        val tvDayNight = view.findViewById<TextView>(R.id.tvDayNight)

        FirebaseDatabase.getInstance()
            .getReference("terrarium")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val airTemp = snapshot.child("tempAir")
                        .getValue(Double::class.java) ?: 0.0

                    val pressure = snapshot.child("pressure")
                        .getValue(Double::class.java) ?: 0.0

                    val soilTemp = snapshot.child("tempSoil")
                        .getValue(Double::class.java) ?: 0.0

                    val moisture = snapshot.child("soilMoisture")
                        .getValue(Int::class.java) ?: 0

                    val mode = snapshot.child("currentMode")
                        .getValue(String::class.java) ?: "auto"

                    // ================= UI =================

                    tvAirTemp.text = "%.1f°C".format(airTemp)
                    tvPressure.text = "%.1f мм рт.ст.".format(pressure)
                    tvSoilTemp.text = "%.1f°C".format(soilTemp)
                    tvSoilMoisture.text = "$moisture%"

                    // 🔥 режим системи
                    tvCurrentMode.text = mode.uppercase()

                    // 🌞/🌙 логіка дня/ночі (локально)
                    tvDayNight.text = getDayNightText(mode)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ================= DAY / NIGHT LOGIC =================
    private fun getDayNightText(mode: String): String {

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isDayNow = hour in 6..18

        return when (mode) {

            "day" -> "☀️ ДЕНЬ"
            "night" -> "🌙 НІЧ"

            "auto" -> {
                if (isDayNow) "☀️ ДЕНЬ"
                else "🌙 НІЧ"
            }

            "manual" -> {
                if (isDayNow) "☀️ ДЕНЬ"
                else "🌙 НІЧ"
            }

            else -> {
                if (isDayNow) "☀️ ДЕНЬ"
                else "🌙 НІЧ"
            }
        }
    }
}