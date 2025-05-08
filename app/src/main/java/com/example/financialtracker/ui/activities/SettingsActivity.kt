package com.example.financialtracker.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.financialtracker.R
import com.example.financialtracker.data.helper.LocaleHelper
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var darkModeSwitch: MaterialSwitch
    private lateinit var notificationsSwitch: MaterialSwitch
    private lateinit var fontSizeSeekBar: Slider
    private lateinit var fontSizeValueText: TextView
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        darkModeSwitch = findViewById(R.id.switch_dark_mode)
        notificationsSwitch = findViewById(R.id.switch_notifications)
        fontSizeSeekBar = findViewById(R.id.font_size_seekbar)
        fontSizeValueText = findViewById(R.id.font_size_value)
        saveButton = findViewById(R.id.save_button)


        darkModeSwitch.isChecked = false
        notificationsSwitch.isChecked = true
        fontSizeSeekBar.value = 16f
        fontSizeValueText.text = "${fontSizeSeekBar.value.toInt()}sp"

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(this, "Dark Mode Enabled", Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(this, "Dark Mode Disabled", Toast.LENGTH_SHORT).show()
            }
        }

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications Disabled", Toast.LENGTH_SHORT).show()
            }
        }

        fontSizeSeekBar.addOnChangeListener { _, value, _ ->
            fontSizeValueText.text = "${value.toInt()}sp"
        }

        saveButton.setOnClickListener {
            onSaveClicked()
        }

        val sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE)
        val currentLanguage = sharedPreferences.getString("app_language", "en") ?: "en"

        val languages = listOf("English", "Bulgarian", "German", "Greek", "Spanish")
        val languageSpinner: Spinner = findViewById(R.id.language_spinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        languageSpinner.setSelection(languages.indexOf("English"))

        languageSpinner.setSelection(
            languages.indexOfFirst {
                when (it) {
                    "Bulgarian" -> "bg"
                    "German" -> "de"
                    "Greek" -> "el"
                    "Spanish" -> "es"
                    else -> "en"
                } == currentLanguage
            }
        )

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLanguage = when (position) {
                    1 -> "bg"
                    2 -> "de"
                    3 -> "el"
                    4 -> "es"
                    else -> "en"
                }

                val sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE)
                val currentLanguage = sharedPreferences.getString("app_language", "en") ?: "en"

                if (selectedLanguage != currentLanguage) {
                    sharedPreferences.edit().putString("app_language", selectedLanguage).apply()

                    LocaleHelper.setLocale(this@SettingsActivity, selectedLanguage)

                    val configuration = resources.configuration
                    Locale.setDefault(Locale(selectedLanguage))
                    configuration.setLocale(Locale(selectedLanguage))
                    resources.updateConfiguration(configuration, resources.displayMetrics)

                    languageSpinner.postDelayed({
                        recreate()
                    }, 200)
                }
            }


            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun onSaveClicked() {
        val isDarkMode = darkModeSwitch.isChecked
        val isNotificationsEnabled = notificationsSwitch.isChecked
        val fontSize = fontSizeSeekBar.value.toInt()

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()

        saveSettings(isDarkMode, isNotificationsEnabled, fontSize)

        finish()
    }

    private fun saveSettings(isDarkMode: Boolean, isNotificationsEnabled: Boolean, fontSize: Int) {
        val sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isDarkMode", isDarkMode)
        editor.putBoolean("isNotificationsEnabled", isNotificationsEnabled)
        editor.putInt("fontSize", fontSize)
        editor.apply()
    }
}