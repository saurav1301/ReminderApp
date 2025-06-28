package com.example.reminderapp

import Reminder
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistorySectionedAdapter(private val items: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false)
            ReminderViewHolder(view)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.header.text = items[position] as String
        } else if (holder is ReminderViewHolder) {
            val reminder = items[position] as Reminder
            holder.title.text = reminder.title
            holder.time.text = reminder.time
            holder.status.text = reminder.status

            // Status chip background
            val bgColor = when (reminder.status) {
                "To-Do" -> Color.parseColor("#F44336")
                "In Progress" -> Color.parseColor("#FFC107")
                "Done" -> Color.parseColor("#4CAF50")
                else -> Color.GRAY
            }

            holder.status.background = GradientDrawable().apply {
                cornerRadius = 12f
                setColor(bgColor)
            }
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val header: TextView = view.findViewById(android.R.id.text1)

        init {
            header.setTextColor(Color.WHITE)
            header.textSize = 18f
        }
    }

    class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.taskTitle)
        val time: TextView = view.findViewById(R.id.taskTime)
        val status: TextView = view.findViewById(R.id.taskStatus)
    }
}
