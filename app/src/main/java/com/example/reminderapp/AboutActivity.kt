package com.example.reminderapp

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val checkBox = findViewById<CheckBox>(R.id.permissionCheckbox)
        val continueBtn = findViewById<Button>(R.id.continueButton)

        continueBtn.setOnClickListener {
            if (!checkBox.isChecked) {
                // checkbox NOT checked → show toast + shake
                Toast.makeText(this, "Please accept the permission to continue", Toast.LENGTH_SHORT).show()
                val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
                checkBox.startAnimation(shake)
            } else {
                // checkbox checked → proceed to next screen
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }
}
