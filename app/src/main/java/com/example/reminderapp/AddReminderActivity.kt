package com.example.reminderapp

import Reminder
import android.app.Activity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AddReminderActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var statusSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var alarmCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminder)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        titleEditText = findViewById(R.id.titleEditText)
        timeEditText = findViewById(R.id.timeEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        dateEditText = findViewById(R.id.dateEditText)
        statusSpinner = findViewById(R.id.statusSpinner)
        saveButton = findViewById(R.id.saveButton)
        alarmCheckBox = findViewById(R.id.alarmCheckBox)

        val statusOptions = arrayOf("To-Do", "In Progress")
        statusSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusOptions)

        val isEditing = intent.hasExtra("edit_index")
        if (isEditing) {
            titleEditText.setText(intent.getStringExtra("reminder_title"))
            timeEditText.setText(intent.getStringExtra("reminder_time"))
            descriptionEditText.setText(intent.getStringExtra("reminder_description"))
            dateEditText.setText(intent.getStringExtra("reminder_date"))
            val status = intent.getStringExtra("reminder_status")
            val index = statusOptions.indexOf(status)
            if (index >= 0) statusSpinner.setSelection(index)
        }

        timeEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val amPm = if (selectedHour >= 12) "PM" else "AM"
                val formattedHour = if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour
                val formattedTime = String.format("%02d:%02d %s", formattedHour, selectedMinute, amPm)
                timeEditText.setText(formattedTime)
            }, hour, minute, false).show()
        }

        dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, y, m, d ->
                val formatted = String.format("%04d-%02d-%02d", y, m + 1, d)
                dateEditText.setText(formatted)
            }, year, month, day).show()
        }

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val time = timeEditText.text.toString()
            val status = statusSpinner.selectedItem.toString()
            val description = descriptionEditText.text.toString()
            val date = dateEditText.text.toString()
            val hasAlarm = alarmCheckBox.isChecked

            if (title.isNotEmpty() && time.isNotEmpty() && date.isNotEmpty()) {
                val reminder = Reminder(title, time, status, description, date, hasAlarm)

                if (hasAlarm) scheduleAlarm(reminder)

                val resultIntent = Intent().apply {
                    putExtra("reminder_title", reminder.title)
                    putExtra("reminder_time", reminder.time)
                    putExtra("reminder_status", reminder.status)
                    putExtra("reminder_description", reminder.description)
                    putExtra("reminder_date", reminder.date)
                    putExtra("reminder_alarm", reminder.hasAlarm)
                }
                if (isEditing) {
                    val index = intent.getIntExtra("edit_index", -1)
                    resultIntent.putExtra("edit_index", index)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scheduleAlarm(reminder: Reminder) {
        val dateTimeStr = "${reminder.date} ${reminder.time}"
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        val calendar = Calendar.getInstance()
        try {
            calendar.time = sdf.parse(dateTimeStr) ?: return
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        val alarmIntent = Intent(this, ReminderAlarmReceiver::class.java).apply {
            putExtra("title", reminder.title)
            putExtra("description", reminder.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            System.currentTimeMillis().toInt(),
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}
