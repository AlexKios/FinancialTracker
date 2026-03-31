package com.example.financialtracker.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.example.financialtracker.R
import com.example.financialtracker.data.model.UserSettings
import com.example.financialtracker.ui.viewmodels.SettingsViewModel
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var darkModeSwitch: MaterialSwitch
    private lateinit var notificationsSwitch: MaterialSwitch
    private lateinit var graphSizeSeekBar: Slider
    private lateinit var graphSizeValueText: TextView
    private lateinit var saveButton: Button
    private lateinit var languageSpinner: Spinner

    private val viewModel: SettingsViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (!isGranted) {
            notificationsSwitch.isChecked = false
            Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        initViews()
        setupLanguageSpinner()
        setupListeners()
        observeViewModel()

        viewModel.loadUserSettings()
    }

    private fun initViews() {
        darkModeSwitch = findViewById(R.id.switch_dark_mode)
        notificationsSwitch = findViewById(R.id.switch_notifications)
        graphSizeSeekBar = findViewById(R.id.graph_size_seekbar)
        graphSizeValueText = findViewById(R.id.graph_size_value)
        saveButton = findViewById(R.id.save_button)
        languageSpinner = findViewById(R.id.language_spinner)

        graphSizeSeekBar.valueFrom = 10f
        graphSizeSeekBar.valueTo = 30f
        graphSizeSeekBar.stepSize = 1f
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.userSettings.collect { settings ->
                settings?.let {
                    darkModeSwitch.isChecked = it.darkMode
                    notificationsSwitch.isChecked = it.notificationsEnabled
                    graphSizeSeekBar.value = it.graphSize.toFloat()
                    graphSizeValueText.text = "${it.graphSize}sp"

                    val languages = mapOf("en" to 0, "bg" to 1, "de" to 2, "el" to 3, "es" to 4)
                    val langIndex = languages[it.language] ?: 0
                    languageSpinner.setSelection(langIndex, false)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.saveSuccess.collect { success ->
                if (success) {
                    applySettings()
                    viewModel.resetSaveSuccess()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@SettingsActivity, "Error: $it", Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun applySettings() {
        val isDarkMode = darkModeSwitch.isChecked
        val selectedLanguage = getSelectedLanguageCode()

        val prefs = getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("darkMode", isDarkMode).apply()

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()

        val currentLang = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: Locale.getDefault().language
        val languageChanged = currentLang != selectedLanguage

        if (languageChanged) {
            val appLocale = LocaleListCompat.forLanguageTags(selectedLanguage)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val themeChanged = (isDarkMode && currentNightMode != Configuration.UI_MODE_NIGHT_YES) ||
                (!isDarkMode && currentNightMode != Configuration.UI_MODE_NIGHT_NO)

        if (themeChanged) {
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        if (themeChanged || languageChanged) {
            setResult(RESULT_OK)
        }
        finish()
    }

    @SuppressLint("SetTextI18n")
    private fun setupListeners() {
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

    private fun onSaveClicked() {
        val settings = UserSettings(
            darkMode = darkModeSwitch.isChecked,
            notificationsEnabled = notificationsSwitch.isChecked,
            graphSize = graphSizeSeekBar.value.toInt(),
            language = getSelectedLanguageCode()
        )
        viewModel.saveUserSettings(settings)
    }

    private fun getSelectedLanguageCode(): String {
        return when (languageSpinner.selectedItem.toString()) {
            "Bulgarian" -> "bg"
            "German" -> "de"
            "Greek" -> "el"
            "Spanish" -> "es"
            else -> "en"
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
