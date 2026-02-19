package com.example.financialtracker.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.financialtracker.R
import com.example.financialtracker.data.helper.LocaleHelper
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import java.util.Locale
import androidx.core.content.edit

class SettingsActivity : AppCompatActivity() {

    private lateinit var darkModeSwitch: MaterialSwitch
    private lateinit var notificationsSwitch: MaterialSwitch
    private lateinit var graphSizeSeekBar: Slider
    private lateinit var graphSizeValueText: TextView
    private lateinit var saveButton: Button

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
                notificationsSwitch.isChecked = true
            } else {
                Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show()
                notificationsSwitch.isChecked = false
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        darkModeSwitch = findViewById(R.id.switch_dark_mode)
        notificationsSwitch = findViewById(R.id.switch_notifications)
        graphSizeSeekBar = findViewById(R.id.graph_size_seekbar)
        graphSizeValueText = findViewById(R.id.graph_size_value)
        saveButton = findViewById(R.id.save_button)

        val sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE)
        
        darkModeSwitch.isChecked = sharedPreferences.getBoolean("isDarkMode", false)
        notificationsSwitch.isChecked = sharedPreferences.getBoolean("isNotificationsEnabled", true)
        graphSizeSeekBar.value = sharedPreferences.getInt("graphSize", 16).toFloat()
        graphSizeValueText.text = "${graphSizeSeekBar.value.toInt()}sp"

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(this, "Dark Mode Enabled", Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(this, "Dark Mode Disabled", Toast.LENGTH_SHORT).show()
            }
        }

        notificationsSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) {
                return@setOnCheckedChangeListener
            }
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Notifications Disabled", Toast.LENGTH_SHORT).show()
            }
        }

        graphSizeSeekBar.addOnChangeListener { _, value, _ ->
            graphSizeValueText.text = "${value.toInt()}sp"
        }

        saveButton.setOnClickListener {
            onSaveClicked()
        }
        
        val currentLanguage = sharedPreferences.getString("app_language", "en") ?: "en"

        val languages = listOf("English", "Bulgarian", "German", "Greek", "Spanish")
        val languageSpinner: Spinner = findViewById(R.id.language_spinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

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
                
                if (selectedLanguage != currentLanguage) {
                    sharedPreferences.edit { putString("app_language", selectedLanguage) }

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

    override fun onResume() {
        super.onResume()
        // Sync notification switch state if permission was revoked from system settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE)
            val notificationsEnabledInPrefs = sharedPreferences.getBoolean("isNotificationsEnabled", true)
            val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            
            if (notificationsEnabledInPrefs && !hasPermission) {
                // If setting is ON but permission is OFF, sync them down
                sharedPreferences.edit { putBoolean("isNotificationsEnabled", false) }
                notificationsSwitch.isChecked = false
            }
        }
    }

    private fun onSaveClicked() {
        val isDarkMode = darkModeSwitch.isChecked
        val isNotificationsEnabled = notificationsSwitch.isChecked
        val graphSize = graphSizeSeekBar.value.toInt()

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()

        saveSettings(isDarkMode, isNotificationsEnabled, graphSize)

        finish()
    }

    private fun saveSettings(isDarkMode: Boolean, isNotificationsEnabled: Boolean, graphSize: Int) {
        val sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE)
        sharedPreferences.edit {
            putBoolean("isDarkMode", isDarkMode)
            putBoolean("isNotificationsEnabled", isNotificationsEnabled)
            putInt("graphSize", graphSize)
        }
    }
}
