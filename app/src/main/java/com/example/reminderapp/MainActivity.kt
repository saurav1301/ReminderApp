package com.example.reminderapp

import Reminder
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var reminderList: RecyclerView
    private lateinit var reminderAdapter: ReminderAdapter
    private val reminders = ArrayList<Reminder>()
    private lateinit var todayTaskCountView: TextView
    private lateinit var selectAllBtn: Button
    private var isAllSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 201)
            }
        }

        val sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val savedName = sharedPreferences.getString("name", null)
        val greetingText: TextView = findViewById(R.id.greetingText)
        greetingText.text = if (savedName.isNullOrEmpty()) {
            "Hello, User!"
        } else {
            "Hello, $savedName!"
        }

        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.selectedItemId = R.id.nav_home

        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        reminderList = findViewById(R.id.taskRecyclerView)
        reminderList.layoutManager = LinearLayoutManager(this)
        todayTaskCountView = findViewById(R.id.todayTaskCount)
        selectAllBtn = findViewById(R.id.btnSelectAll)

        loadRemindersFromFile()
        reminders.sortBy { it.date }

        reminderAdapter = ReminderAdapter(
            reminders,
            onItemCheckedChange = {
                toggleActionButtons()
                val anySelected = reminders.any { it.isSelected }
                reminderAdapter.multiSelectMode = anySelected
            },
            multiSelectMode = false,
            onSwipeDelete = { deletedReminder, position ->
                reminders.removeAt(position)
                reminderAdapter.notifyItemRemoved(position)
                saveRemindersToFile()
                updateTodayTaskCount()
                Snackbar.make(reminderList, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        reminders.add(position, deletedReminder)
                        reminderAdapter.notifyItemInserted(position)
                        saveRemindersToFile()
                        updateTodayTaskCount()
                    }.show()
            },
            onSwipeDone = { position ->
                reminders[position] = reminders[position].copy(status = "Done")
                reminderAdapter.notifyItemChanged(position)
                saveRemindersToFile()
                updateTodayTaskCount()
            },
            onItemClick = { reminder ->
                if (!reminderAdapter.multiSelectMode) {
                    val intent = Intent(this, ReminderDetailActivity::class.java)
                    intent.putExtra("title", reminder.title)
                    intent.putExtra("time", reminder.time)
                    intent.putExtra("status", reminder.status)
                    intent.putExtra("description", reminder.description)
                    intent.putExtra("date", reminder.date)
                    intent.putExtra("edit_index", reminders.indexOf(reminder))
                    startActivityForResult(intent, 103)
                }
            }
        )
        reminderList.adapter = reminderAdapter
        reminderAdapter.getItemTouchHelper(this).attachToRecyclerView(reminderList)

        val doneBtn = findViewById<Button>(R.id.btnDoneSelected)
        val deleteBtn = findViewById<Button>(R.id.btnDeleteSelected)

        doneBtn.setOnClickListener {
            var anyChanged = false
            for (i in reminders.indices) {
                if (reminders[i].isSelected) {
                    reminders[i] = reminders[i].copy(status = "Done", isSelected = false)
                    anyChanged = true
                }
            }
            if (anyChanged) {
                reminderAdapter.multiSelectMode = false
                reminderAdapter.notifyDataSetChanged()
                saveRemindersToFile()
                updateTodayTaskCount()
            }
            toggleActionButtons()
        }

        deleteBtn.setOnClickListener {
            val deletedItems = reminders.filter { it.isSelected }
            val deletedPositions = deletedItems.map { reminders.indexOf(it) }
            if (deletedItems.isNotEmpty()) {
                val deletedSet = deletedItems.toMutableList()
                deletedPositions.sortedDescending().forEach { pos ->
                    reminderAdapter.animateSwipeDelete(pos)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    deletedPositions.sortedDescending().forEach { pos ->
                        reminders.removeAt(pos)
                    }
                    reminderAdapter.multiSelectMode = false
                    reminderAdapter.notifyDataSetChanged()
                    saveRemindersToFile()
                    toggleActionButtons()
                    updateTodayTaskCount()
                    Snackbar.make(reminderList, "Tasks deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            for (i in deletedItems.indices) {
                                reminders.add(deletedPositions[i], deletedItems[i])
                            }
                            reminderAdapter.notifyDataSetChanged()
                            saveRemindersToFile()
                            updateTodayTaskCount()
                        }.show()
                }, 300)
            }
        }

        selectAllBtn.setOnClickListener {
            if (!isAllSelected) {
                reminderAdapter.selectAll()
                isAllSelected = true
                selectAllBtn.text = "Clear"
            } else {
                reminderAdapter.clearSelection()
                isAllSelected = false
                selectAllBtn.text = "Select All"
            }
        }

        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, AddReminderActivity::class.java)
            startActivityForResult(intent, 101)
        }

        updateTodayTaskCount()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            val title = data?.getStringExtra("reminder_title") ?: return
            val time = data.getStringExtra("reminder_time") ?: return
            val description = data.getStringExtra("reminder_description") ?: ""
            val status = data.getStringExtra("reminder_status") ?: return
            val date = data.getStringExtra("reminder_date") ?: return

            val hasAlarm = data.getBooleanExtra("reminder_alarm", false)
            val newReminder = Reminder(title, time, status, description, date, hasAlarm)
            reminders.add(0, newReminder)
            reminders.sortBy { it.date }
            reminderAdapter.notifyDataSetChanged()
            saveRemindersToFile()
            updateTodayTaskCount()
        }
        if (requestCode == 102 && resultCode == Activity.RESULT_OK) {
            val index = data?.getIntExtra("edit_index", -1) ?: -1
            if (index != -1) {
                val title = data?.getStringExtra("reminder_title") ?: return
                val time = data.getStringExtra("reminder_time") ?: return
                val status = data.getStringExtra("reminder_status") ?: return
                val description = data.getStringExtra("reminder_description") ?: ""
                val date = data.getStringExtra("reminder_date") ?: return

                reminders[index] = Reminder(title, time, status, description, date)
                reminders.sortBy { it.date }
                reminderAdapter.notifyDataSetChanged()
                saveRemindersToFile()
                Toast.makeText(this, "Reminder Updated", Toast.LENGTH_SHORT).show()
                updateTodayTaskCount()
            }
        }
        if (requestCode == 103 && resultCode == Activity.RESULT_OK && data != null) {
            val index = data.getIntExtra("edit_index", -1)
            if (index != -1) {
                val updatedReminder = Reminder(
                    data.getStringExtra("reminder_title") ?: "",
                    data.getStringExtra("reminder_time") ?: "",
                    data.getStringExtra("reminder_status") ?: "",
                    data.getStringExtra("reminder_description") ?: "",
                    data.getStringExtra("reminder_date") ?: ""
                )
                reminders[index] = updatedReminder
                reminders.sortBy { it.date }
                reminderAdapter.notifyDataSetChanged()
                saveRemindersToFile()
                updateTodayTaskCount()
                Toast.makeText(this, "Reminder Updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleActionButtons() {
        val actionBar = findViewById<LinearLayout>(R.id.actionBar)
        val selectedCount = reminders.count { it.isSelected }
        if (selectedCount == 0) {
            reminderAdapter.multiSelectMode = false
        }
        actionBar.visibility = if (reminderAdapter.multiSelectMode && selectedCount > 0) View.VISIBLE else View.GONE
    }

    private fun updateTodayTaskCount() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayTasks = reminders.count {
            it.status != "Done" && it.date == today
        }
        todayTaskCountView.text = "Today's Tasks: $todayTasks"
    }

    private fun saveRemindersToFile() {
        try {
            val gson = com.google.gson.Gson()
            val json = gson.toJson(reminders)
            val file = File(getExternalFilesDir(null), "reminders.json")
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadRemindersFromFile() {
        try {
            val file = File(getExternalFilesDir(null), "reminders.json")
            if (file.exists()) {
                val json = file.readText()
                val type = object : com.google.gson.reflect.TypeToken<List<Reminder>>() {}.type
                val loadedList: List<Reminder> = com.google.gson.Gson().fromJson(json, type)

                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // Only keep reminders for today or future
                val upcomingReminders = loadedList.filter { it.date >= today }

                reminders.clear()
                reminders.addAll(upcomingReminders)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
