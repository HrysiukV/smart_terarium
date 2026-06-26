package com.example.terrariumcontrol111.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.terrariumcontrol111.R
import java.text.SimpleDateFormat
import java.util.*

class LogAdapter(private var list: List<LogEvent>) :
    RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val time: TextView = view.findViewById(R.id.txtTime)
        val device: TextView = view.findViewById(R.id.txtDevice)
        val reason: TextView = view.findViewById(R.id.txtReason)
        val action: TextView = view.findViewById(R.id.txtAction)
        val root: LinearLayout = view.findViewById(R.id.logItemRoot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.log_item, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val item = list[position]

        val date = Date(item.timestamp * 1000)
        val format = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
        holder.time.text = "🕐 ${format.format(date)}"
        holder.reason.text = item.reason

        if (item.type == "mode") {
            // РЕЖИМ
            val (emoji, label, color) = when (item.action.lowercase()) {
                "manual" -> Triple("🎮", "MANUAL", "#FFFFFF")
                "day"    -> Triple("☀️", "ДЕНЬ",   "#FFD600")
                "night"  -> Triple("🌙", "НІЧ",    "#7986CB")
                else     -> Triple("•", item.action.uppercase(), "#AAAAAA")
            }
            holder.device.text = "$emoji $label"
            holder.device.setTextColor(Color.parseColor(color))
            holder.root.setBackgroundResource(R.drawable.log_item_bg_default)

            holder.action.text = "РЕЖИМ"
            holder.action.setTextColor(Color.parseColor(color))
            holder.action.setBackgroundResource(R.drawable.log_item_bg_default)

        } else {
            // ПРИСТРІЙ — стара логіка
            val emoji = when (item.device.lowercase()) {
                "heat"  -> "🔥"
                "light" -> "💡"
                "pump"  -> "💧"
                else    -> "•"
            }
            holder.device.text = "$emoji ${item.device.uppercase()}"

            holder.action.text = item.action.uppercase()
            holder.action.setBackgroundResource(
                when (item.action.lowercase()) {
                    "on"  -> R.drawable.log_pill_on
                    "off" -> R.drawable.log_pill_off
                    else  -> R.drawable.log_item_bg_default
                }
            )

            when (item.device.lowercase()) {
                "heat" -> {
                    holder.device.setTextColor(Color.parseColor("#FF6D00"))
                    holder.root.setBackgroundResource(R.drawable.log_item_bg_heat)
                }
                "light" -> {
                    holder.device.setTextColor(Color.parseColor("#F5C842"))
                    holder.root.setBackgroundResource(R.drawable.log_item_bg_light)
                }
                "pump" -> {
                    holder.device.setTextColor(Color.parseColor("#42A5F5"))
                    holder.root.setBackgroundResource(R.drawable.log_item_bg_pump)
                }
                else -> {
                    holder.device.setTextColor(Color.WHITE)
                    holder.root.setBackgroundResource(R.drawable.log_item_bg_default)
                }
            }
        }
    }

    override fun getItemCount() = list.size

    fun update(newList: List<LogEvent>) {
        list = newList
        notifyDataSetChanged()
    }
}