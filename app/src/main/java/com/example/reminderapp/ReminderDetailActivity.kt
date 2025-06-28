package com.example.reminderapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ReminderDetailActivity : AppCompatActivity() {

    private var editIndex: Int = -1

    private lateinit var valueTitle: TextView
    private lateinit var valueTime: TextView
    private lateinit var valueStatus: TextView
    private lateinit var valueDescription: TextView
    private lateinit var valueDate: TextView

    private lateinit var title: String
    private lateinit var time: String
    private lateinit var status: String
    private lateinit var description: String
    private lateinit var date: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_detail)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Get index if editing
        editIndex = intent.getIntExtra("edit_index", -1)

        // Bind views
        valueTitle = findViewById(R.id.valueTitle)
        valueTime = findViewById(R.id.valueTime)
        valueStatus = findViewById(R.id.valueStatus)
        valueDescription = findViewById(R.id.valueDescription)
        valueDate = findViewById(R.id.valueDate)

        // Get data
        title = intent.getStringExtra("title") ?: "No Title"
        time = intent.getStringExtra("time") ?: "No Time"
        status = intent.getStringExtra("status") ?: "No Status"
        description = intent.getStringExtra("description") ?: "No Description"
        date = intent.getStringExtra("date") ?: "No Date"

        // Set values
        valueTitle.text = title
        valueTime.text = time
        valueStatus.text = status
        valueDescription.text = description
        valueDate.text = date
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.reminder_detail_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_edit) {
            val intent = Intent(this, AddReminderActivity::class.java)
            intent.putExtra("edit_index", editIndex)
            intent.putExtra("reminder_title", valueTitle.text.toString())
            intent.putExtra("reminder_time", valueTime.text.toString())
            intent.putExtra("reminder_status", valueStatus.text.toString())
            intent.putExtra("reminder_description", valueDescription.text.toString())
            intent.putExtra("reminder_date", valueDate.text.toString())
            startActivityForResult(intent, 102)
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 102 && resultCode == Activity.RESULT_OK && data != null) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
