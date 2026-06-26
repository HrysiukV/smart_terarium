package com.example.terrariumcontrol111.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.terrariumcontrol111.model.SensorData
import com.google.firebase.database.*

class MainViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()
    private val terrariumRef = database.getReference("terrarium")

    private val _sensorHistory = MutableLiveData<List<SensorData>>()
    val sensorHistory: LiveData<List<SensorData>> = _sensorHistory

    private var historyListener: ValueEventListener? = null

    init {
        cleanupHistory()
    }

    // ================= LIVE LISTENER =================
    fun startSensorHistoryListener(days: Int) {

        val ref = terrariumRef.child("history")

        val fromTimestamp = System.currentTimeMillis() / 1000 - days * 86400L

        val query = ref.orderByChild("timestamp")
            .startAt(fromTimestamp.toDouble())

        // remove old listener
        historyListener?.let {
            ref.removeEventListener(it)
        }

        historyListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val list = mutableListOf<SensorData>()

                for (item in snapshot.children) {
                    val data = item.getValue(SensorData::class.java)
                    if (data != null) list.add(data)
                }

                list.sortBy { it.timestamp }

                // limit 500 points
                val result = if (list.size > 500) {
                    val step = list.size / 500
                    list.filterIndexed { i, _ -> i % step == 0 }
                } else list

                _sensorHistory.postValue(result)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        query.addValueEventListener(historyListener!!)
    }

    // ================= CLEANUP =================
    private fun cleanupHistory() {

        val now = System.currentTimeMillis() / 1000
        val day7 = now - 7 * 86400L
        val day30 = now - 30 * 86400L

        val ref = terrariumRef.child("history")

        ref.orderByChild("timestamp")
            .endAt(day30.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    for (item in snapshot.children) {
                        item.ref.removeValue()
                    }

                    thinOutOldData(day7, day30)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ================= THIN OUT =================
    private fun thinOutOldData(day7: Long, day30: Long) {

        val ref = terrariumRef.child("history")

        ref.orderByChild("timestamp")
            .startAt(day30.toDouble())
            .endAt(day7.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val children = snapshot.children.toList()

                    children.forEachIndexed { index, item ->
                        if (index % 40 != 0) {
                            item.ref.removeValue()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onCleared() {
        super.onCleared()

        // IMPORTANT: remove listener to avoid leaks
        historyListener?.let {
            terrariumRef.child("history").removeEventListener(it)
        }
    }
}