package com.example.terrariumcontrol111.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.terrariumcontrol111.R
import com.google.android.material.chip.Chip
import com.google.firebase.database.*

class LogFragment : Fragment(R.layout.fragment_log) {

    private lateinit var adapter: LogAdapter

    private var allLogs = listOf<LogEvent>()
    private var currentFilter = "ALL"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerViewLog)
        val debug = view.findViewById<TextView>(R.id.txtDebug)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = LogAdapter(mutableListOf())
        recycler.adapter = adapter

        fun select(filter: String, chipId: Int) {
            currentFilter = filter
            updateChipUI(view, chipId)
            applyFilter()
        }

        view.findViewById<Chip>(R.id.chipAll).setOnClickListener { select("ALL", R.id.chipAll) }
        view.findViewById<Chip>(R.id.chipOn).setOnClickListener { select("ON", R.id.chipOn) }
        view.findViewById<Chip>(R.id.chipOff).setOnClickListener { select("OFF", R.id.chipOff) }
        view.findViewById<Chip>(R.id.chipLight).setOnClickListener { select("LIGHT", R.id.chipLight) }
        view.findViewById<Chip>(R.id.chipPump).setOnClickListener { select("PUMP", R.id.chipPump) }
        view.findViewById<Chip>(R.id.chipHeat).setOnClickListener { select("HEAT", R.id.chipHeat) }

        val ref = FirebaseDatabase.getInstance()
            .getReference("terrarium")
            .child("logs")

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val list = mutableListOf<LogEvent>()

                for (child in snapshot.children) {

                    val action = child.child("action")
                        .getValue(String::class.java)
                        ?.trim() ?: ""

                    val device = child.child("device")
                        .getValue(String::class.java)
                        ?.trim()
                        ?.lowercase() ?: ""

                    val reason = child.child("reason")
                        .getValue(String::class.java)
                        ?.trim() ?: ""

                    val timestamp = child.child("timestamp")
                        .value
                        ?.toString()
                        ?.trim()
                        ?.toLongOrNull() ?: 0L

                    val type = child.child("type")
                        .getValue(String::class.java)
                        ?.trim() ?: "device"

                    list.add(LogEvent(action, device, reason, timestamp, type))
                }

                allLogs = list.sortedByDescending { it.timestamp }
                debug.text = "TOTAL: ${allLogs.size}"
                applyFilter()
            }

            override fun onCancelled(error: DatabaseError) {
                debug.text = "ERROR: ${error.message}"
            }
        })
    }

    private fun applyFilter() {

        val filtered = when (currentFilter) {
            "ON"    -> allLogs.filter { it.action.equals("on", true) }
            "OFF"   -> allLogs.filter { it.action.equals("off", true) }
            "LIGHT" -> allLogs.filter { it.device.equals("light", true) }
            "PUMP"  -> allLogs.filter { it.device.equals("pump", true) }
            "HEAT"  -> allLogs.filter { it.device.equals("heat", true) }
            else    -> allLogs
        }

        adapter.update(filtered)
    }

    private fun updateChipUI(view: View, selectedId: Int) {

        val chips = listOf(
            R.id.chipAll,
            R.id.chipOn,
            R.id.chipOff,
            R.id.chipLight,
            R.id.chipPump,
            R.id.chipHeat
        )

        chips.forEach { id ->
            val chip = view.findViewById<Chip>(id)
            chip?.isChecked = (id == selectedId)
        }
    }
}