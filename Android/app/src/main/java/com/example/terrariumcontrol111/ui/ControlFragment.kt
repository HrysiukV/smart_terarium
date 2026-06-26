package com.example.terrariumcontrol111.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.terrariumcontrol111.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.*

class ControlFragment : Fragment() {

    private val terrariumRef = FirebaseDatabase.getInstance().getReference("terrarium")

    private lateinit var btnLight: MaterialButton
    private lateinit var btnHeat: MaterialButton
    private lateinit var btnPump: MaterialButton

    private lateinit var btnManual: MaterialButton
    private lateinit var btnDay: MaterialButton
    private lateinit var btnNight: MaterialButton

    private var currentMode = "manual"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // ===== INIT BUTTONS =====
        btnLight = view.findViewById(R.id.btnLight)
        btnHeat = view.findViewById(R.id.btnHeat)
        btnPump = view.findViewById(R.id.btnPump)

        btnManual = view.findViewById(R.id.btnManual)
        btnDay = view.findViewById(R.id.btnDay)
        btnNight = view.findViewById(R.id.btnNight)

        // ===== MODE BUTTONS =====
        btnManual.setOnClickListener { setMode("manual") }
        btnDay.setOnClickListener { setMode("day") }
        btnNight.setOnClickListener { setMode("night") }

        // ===== RELAYS =====
        btnLight.setOnClickListener { toggleRelay("relay1") }
        btnHeat.setOnClickListener { toggleRelay("relay2") }
        btnPump.setOnClickListener { toggleRelay("relay3") }

        // ===== FIREBASE MODE LISTENER =====
        terrariumRef.child("currentMode")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentMode = snapshot.getValue(String::class.java) ?: "manual"
                    updateModeUI()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // ===== RELAY LISTENERS =====
        listenRelay("relay1", btnLight)
        listenRelay("relay2", btnHeat)
        listenRelay("relay3", btnPump)
    }

    // ================= MODE =================

    private fun setMode(mode: String) {
        terrariumRef.child("currentMode").setValue(mode)
        currentMode = mode
        updateModeUI()
    }

    private fun updateModeUI() {

        btnManual.isSelected = currentMode == "manual"
        btnDay.isSelected = currentMode == "day"
        btnNight.isSelected = currentMode == "night"

        updateButtonColors()
    }
    private fun updateButtonColors() {

        setModeColor(btnManual, currentMode == "manual", "#00E676")
        setModeColor(btnDay, currentMode == "day", "#FF9800")
        setModeColor(btnNight, currentMode == "night", "#2196F3")
    }

    private fun setModeColor(btn: MaterialButton, active: Boolean, color: String) {

        if (active) {
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(color)))
            btn.setTextColor(android.graphics.Color.BLACK)
        } else {
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1E1E1E")))
            btn.setTextColor(android.graphics.Color.parseColor(color))
        }
    }

    private fun updateDeviceUI() {
        // UI буде оновлюватись через isSelected (як у тебе вже в listenRelay)
    }

    // ================= RELAYS =================

    private fun toggleRelay(relay: String) {

        if (currentMode != "manual") {
            Toast.makeText(requireContext(), "Тільки MANUAL режим", Toast.LENGTH_SHORT).show()
            return
        }

        terrariumRef.child(relay).runTransaction(object : Transaction.Handler {

            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val current = currentData.getValue(Boolean::class.java) ?: false
                currentData.value = !current
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                if (error != null) {
                    Toast.makeText(
                        requireContext(),
                        "Помилка: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun listenRelay(path: String, button: MaterialButton) {
        terrariumRef.child(path).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val state = snapshot.getValue(Boolean::class.java) ?: false

                button.isSelected = state

                updateDeviceStyle(button, state)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun updateDeviceStyle(button: MaterialButton, active: Boolean) {

        when (button.id) {

            R.id.btnLight -> {
                styleDevice(button, active, "#00E676")
            }

            R.id.btnHeat -> {
                styleDevice(button, active, "#FF5722")
            }

            R.id.btnPump -> {
                styleDevice(button, active, "#2196F3")
            }
        }
    }
    private fun styleDevice(btn: MaterialButton, active: Boolean, color: String) {

        if (active) {
            btn.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor(color)
                )
            )
            btn.setTextColor(android.graphics.Color.BLACK)
        } else {
            btn.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#1E1E1E")
                )
            )
            btn.setTextColor(android.graphics.Color.WHITE)
        }
    }
}