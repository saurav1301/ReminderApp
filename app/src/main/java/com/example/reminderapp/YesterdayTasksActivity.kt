package com.example.reminderapp

import Reminder
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class YesterdayTasksActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var infoText: TextView
    private lateinit var adapter: ReminderAdapter
    private val allReminders = ArrayList<Reminder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yesterday_tasks)

        val toolbar = findViewById<Toolbar>(R.id.toolbarYesterday)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        recyclerView = findViewById(R.id.yesterdayRecyclerView)
        infoText = findViewById(R.id.yesterdayInfoText)

        recyclerView.layoutManager = LinearLayoutManager(this)

        loadReminders()

        val yesterdayDate = getFormattedDate(-1)

        val yesterdayTasks = allReminders.filter { it.date == yesterdayDate }

        if (yesterdayTasks.isEmpty()) {
            Toast.makeText(this, "No tasks from yesterday.", Toast.LENGTH_SHORT).show()
        }

        adapter = ReminderAdapter(
            yesterdayTasks.toMutableList(),
            onItemCheckedChange = {},
            multiSelectMode = false,
            onSwipeDelete = { _, _ -> },
            onSwipeDone = {},
            onItemClick = {}
        )
        recyclerView.adapter = adapter
    }

    private fun getFormattedDate(offset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, offset)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    private fun loadReminders() {
        try {
            val file = File(getExternalFilesDir(null), "reminders.json")
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<Reminder>>() {}.type
                val loadedList: List<Reminder> = Gson().fromJson(json, type)

                val yesterday = getFormattedDate(-1)
                val validDates = setOf(yesterday, getFormattedDate(0), getFormattedDate(1))

                val filteredList = loadedList.filter { it.date in validDates }

                // Overwrite saved file with only recent tasks
                file.writeText(Gson().toJson(filteredList))

                allReminders.clear()
                allReminders.addAll(filteredList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
