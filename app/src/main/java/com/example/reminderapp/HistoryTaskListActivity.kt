package com.example.reminderapp

import Reminder
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class HistoryTaskListActivity : AppCompatActivity() {

    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var adapter: ReminderAdapter
    private val allReminders = ArrayList<Reminder>()
    private lateinit var taskCountText: TextView
    private lateinit var emptyMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_task_list)

        val selectedDate = intent.getStringExtra("selected_date")

        // Toolbar back arrow
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.historyToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (selectedDate == null) {
            Toast.makeText(this, "No date selected.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        taskCountText = findViewById(R.id.historyTaskCount)
        emptyMessage = findViewById(R.id.emptyMessage)

        loadRemindersFromFile()

        val filteredReminders = allReminders.filter { it.date == selectedDate }

        if (filteredReminders.isEmpty()) {
            emptyMessage.visibility = View.VISIBLE
            taskCountText.text = "Tasks: 0"
        } else {
            emptyMessage.visibility = View.GONE
            taskCountText.text = "Tasks: ${filteredReminders.size}"
        }


        adapter = ReminderAdapter(
            filteredReminders.toMutableList(),
            onItemCheckedChange = {},
            multiSelectMode = false,
            onSwipeDelete = { _, _ -> },
            onSwipeDone = {},
            onItemClick = {}
        )
        historyRecyclerView.adapter = adapter
    }

    private fun loadRemindersFromFile() {
        try {
            val file = File(getExternalFilesDir(null), "reminders.json")
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<Reminder>>() {}.type
                val loadedList: List<Reminder> = Gson().fromJson(json, type)
                allReminders.clear()
                allReminders.addAll(loadedList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
