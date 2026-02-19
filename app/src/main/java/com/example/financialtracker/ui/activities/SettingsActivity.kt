package com.example.financialtracker.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
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
import com.example.financialtracker.data.model.UserSettings
import com.example.financialtracker.data.repositories.UserRepository
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var darkModeSwitch: MaterialSwitch
    private lateinit var notificationsSwitch: MaterialSwitch
    private lateinit var graphSizeSeekBar: Slider
    private lateinit var graphSizeValueText: TextView
    private lateinit var saveButton: Button
    private lateinit var languageSpinner: Spinner

    private val userRepository = UserRepository()

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (!isGranted) {
            notificationsSwitch.isChecked = false
            Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load and apply the saved theme before displaying the content
        loadAndApplyTheme()

        setContentView(R.layout.settings)

        // Initialize views
        darkModeSwitch = findViewById(R.id.switch_dark_mode)
        notificationsSwitch = findViewById(R.id.switch_notifications)
        graphSizeSeekBar = findViewById(R.id.graph_size_seekbar)
        graphSizeValueText = findViewById(R.id.graph_size_value)
        saveButton = findViewById(R.id.save_button)
        languageSpinner = findViewById(R.id.language_spinner)

        graphSizeSeekBar.valueFrom = 10f
        graphSizeSeekBar.valueTo = 30f
        graphSizeSeekBar.stepSize = 1f

        // Load all user settings and update the UI controls
        loadUserSettings()

        // Set up listeners
        setupListeners()
        setupLanguageSpinner()
    }

    private fun setupListeners() {
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Instantly apply the theme for preview
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        notificationsSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener

            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (!hasNotificationPermission()) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }

        graphSizeSeekBar.addOnChangeListener { _, value, _ ->
            graphSizeValueText.text = "${value.toInt()}sp"
        }

        saveButton.setOnClickListener {
            onSaveClicked()
        }
    }

    private fun setupLanguageSpinner() {
        val languages = listOf("English", "Bulgarian", "German", "Greek", "Spanish")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter
    }

    private fun loadAndApplyTheme() {
        // Blocking read for theme to prevent UI flicker on start
        userRepository.getUserSettings(onSuccess = { settings ->
            if (settings.isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }, onFailure = {
            // Default to light mode on failure
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        })
    }

    private fun loadUserSettings() {
        userRepository.getUserSettings(
            onSuccess = { settings ->
                // Set dark mode switch based on saved state
                darkModeSwitch.isChecked = settings.isDarkMode

                // Set notification switch based on saved state
                notificationsSwitch.isChecked = settings.isNotificationsEnabled

                graphSizeSeekBar.value = settings.graphSize.toFloat()
                graphSizeValueText.text = "${settings.graphSize}sp"

                val languages = mapOf("en" to 0, "bg" to 1, "de" to 2, "el" to 3, "es" to 4)
                val langIndex = languages[settings.language] ?: 0
                languageSpinner.setSelection(langIndex, false)
            },
            onFailure = { e ->
                Toast.makeText(this, "Failed to load settings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun onSaveClicked() {
        val isDarkMode = darkModeSwitch.isChecked
        val isNotificationsEnabled = notificationsSwitch.isChecked
        val graphSize = graphSizeSeekBar.value.toInt()
        val selectedLanguage = when (languageSpinner.selectedItem.toString()) {
            "Bulgarian" -> "bg"
            "German" -> "de"
            "Greek" -> "el"
            "Spanish" -> "es"
            else -> "en"
        }

        val settings = UserSettings(isDarkMode, isNotificationsEnabled, graphSize, selectedLanguage)

        userRepository.saveUserSettings(settings,
            onSuccess = {
                Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()

                // Check if a recreate is needed for theme or language changes
                val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                val themeChanged = (isDarkMode && currentNightMode != Configuration.UI_MODE_NIGHT_YES) ||
                                   (!isDarkMode && currentNightMode != Configuration.UI_MODE_NIGHT_NO)
                val languageChanged = Locale.getDefault().language != selectedLanguage

                if (themeChanged || languageChanged) {
                    recreate()
                } else {
                    finish()
                }
            },
            onFailure = { e ->
                Toast.makeText(this, "Failed to save settings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No special permission needed for older Android versions
        }
    }
}
