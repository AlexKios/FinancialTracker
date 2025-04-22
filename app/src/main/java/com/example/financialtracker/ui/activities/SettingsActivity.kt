package com.example.financialtracker.ui.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financialtracker.R
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider

class SettingsActivity : AppCompatActivity() {

    // Declare the UI elements
    private lateinit var darkModeSwitch: MaterialSwitch
    private lateinit var notificationsSwitch: MaterialSwitch
    private lateinit var fontSizeSeekBar: Slider
    private lateinit var fontSizeValueText: TextView
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings) // Replace with your actual XML layout file name

        // Initialize the UI elements
        darkModeSwitch = findViewById(R.id.switch_dark_mode)
        notificationsSwitch = findViewById(R.id.switch_notifications)
        fontSizeSeekBar = findViewById(R.id.font_size_seekbar)
        fontSizeValueText = findViewById(R.id.font_size_value)
        saveButton = findViewById(R.id.save_button)

        // Set initial values (these can be saved in SharedPreferences or a database)
        darkModeSwitch.isChecked = false // Example value; replace with actual saved preference
        notificationsSwitch.isChecked = true // Example value; replace with actual saved preference
        fontSizeSeekBar.value = 16f // Example value; replace with actual saved preference
        fontSizeValueText.text = "${fontSizeSeekBar.value.toInt()}sp"

        // Set listeners for the components
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle dark mode change (e.g., toggle app theme)
            // For now, just show a toast
            if (isChecked) {
                Toast.makeText(this, "Dark Mode Enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Dark Mode Disabled", Toast.LENGTH_SHORT).show()
            }
        }

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle notifications switch (e.g., enable/disable notifications)
            if (isChecked) {
                Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications Disabled", Toast.LENGTH_SHORT).show()
            }
        }

        fontSizeSeekBar.addOnChangeListener { _, value, _ ->
            // Update the font size value display when the slider is changed
            fontSizeValueText.text = "${value.toInt()}sp"
        }

        saveButton.setOnClickListener {
            onSaveClicked()
        }
    }

    private fun onSaveClicked() {
        // Get the current settings values
        val isDarkMode = darkModeSwitch.isChecked
        val isNotificationsEnabled = notificationsSwitch.isChecked
        val fontSize = fontSizeSeekBar.value.toInt()

        // Show a toast to confirm the settings have been saved
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()

        // You can save these settings in SharedPreferences, a database, or apply them to the app
        saveSettings(isDarkMode, isNotificationsEnabled, fontSize)

        // Optionally, navigate back to the previous screen or home activity
        finish()
    }

    private fun saveSettings(isDarkMode: Boolean, isNotificationsEnabled: Boolean, fontSize: Int) {
        // Save settings using SharedPreferences or other storage methods
        // Example: SharedPreferences (Not implemented in this code, but you can add it)
        val sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isDarkMode", isDarkMode)
        editor.putBoolean("isNotificationsEnabled", isNotificationsEnabled)
        editor.putInt("fontSize", fontSize)
        editor.apply()
    }
}