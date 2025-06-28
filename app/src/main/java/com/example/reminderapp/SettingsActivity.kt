package com.example.reminderapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences

    // Form views
    private lateinit var formLayout: LinearLayout
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var occupationInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var btnSave: Button

    // Display views
    private lateinit var profileLayout: LinearLayout
    private lateinit var nameDisplay: TextView
    private lateinit var emailDisplay: TextView
    private lateinit var phoneDisplay: TextView
    private lateinit var occupationDisplay: TextView
    private lateinit var cityDisplay: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPref = getSharedPreferences("user_profile", MODE_PRIVATE)

        // Form views
        formLayout = findViewById(R.id.formLayout)
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        occupationInput = findViewById(R.id.occupationInput)
        cityInput = findViewById(R.id.cityInput)
        btnSave = findViewById(R.id.btnSave)

        // Display views
        profileLayout = findViewById(R.id.profileLayout)
        nameDisplay = findViewById(R.id.nameDisplay)
        emailDisplay = findViewById(R.id.emailDisplay)
        phoneDisplay = findViewById(R.id.phoneDisplay)
        occupationDisplay = findViewById(R.id.occupationDisplay)
        cityDisplay = findViewById(R.id.cityDisplay)

        // Check if profile is already saved
        if (sharedPref.contains("name")) {
            showProfile()
        } else {
            showForm()
        }

        btnSave.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val occupation = occupationInput.text.toString().trim()
            val city = cityInput.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || occupation.isEmpty() || city.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            with(sharedPref.edit()) {
                putString("name", name)
                putString("email", email)
                putString("phone", phone)
                putString("occupation", occupation)
                putString("city", city)
                apply()
            }

            showProfile()
        }

        // Bottom navigation
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.selectedItemId = R.id.nav_settings
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    finish()
                    true
                }
                else -> true
            }
        }
        val btnYesterday = findViewById<Button>(R.id.btnYesterdayTasks)
        btnYesterday.setOnClickListener {
            startActivity(Intent(this, YesterdayTasksActivity::class.java))
        }

    }

    private fun showForm() {
        formLayout.visibility = LinearLayout.VISIBLE
        profileLayout.visibility = LinearLayout.GONE
    }

    private fun showProfile() {
        nameDisplay.text = sharedPref.getString("name", "")
        emailDisplay.text = sharedPref.getString("email", "")
        phoneDisplay.text = sharedPref.getString("phone", "")
        occupationDisplay.text = sharedPref.getString("occupation", "")
        cityDisplay.text = sharedPref.getString("city", "")

        formLayout.visibility = LinearLayout.GONE
        profileLayout.visibility = LinearLayout.VISIBLE
    }
}
