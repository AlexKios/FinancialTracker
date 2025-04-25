package com.example.financialtracker.ui.activities

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.financialtracker.R

class AddIncomeActivity : AppCompatActivity() {

    // Declare the UI elements
    private lateinit var amountEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var recurringCheckBox: CheckBox
    private lateinit var addIncomeButton: Button
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_income)

        // Initialize UI elements
        amountEditText = findViewById(R.id.editTextNumber2)
        categorySpinner = findViewById(R.id.spinner2)
        recurringCheckBox = findViewById(R.id.checkBox2)
        addIncomeButton = findViewById(R.id.button4)
        toolbar = findViewById(R.id.toolbar2)

        // Set up the toolbar (if needed for navigation, such as a back button)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set click listener for the Add Income button
        addIncomeButton.setOnClickListener {
            onAddIncomeClicked()
        }
    }

    // Handle the Add Income button click
    private fun onAddIncomeClicked() {
        // Get input values from EditText, Spinner, and CheckBox
        val amount = amountEditText.text.toString()
        val category = categorySpinner.selectedItem.toString()
        val isRecurring = recurringCheckBox.isChecked

        // Basic validation for the amount field
        if (amount.isEmpty()) {
            Toast.makeText(this, "Amount is required", Toast.LENGTH_SHORT).show()
        } else {
            try {
                val amountValue = amount.toDouble()
                if (amountValue <= 0) {
                    Toast.makeText(this, "Amount must be greater than zero", Toast.LENGTH_SHORT).show()
                } else {
                    // Logic for adding the income (e.g., save to database or API)
                    saveIncome(amountValue, category, isRecurring)
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid amount entered", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Save the income (you can replace this with your actual saving logic, like database or API)
    private fun saveIncome(amount: Double, category: String, isRecurring: Boolean) {
        // For now, just show a Toast message with the details
        val recurringStatus = if (isRecurring) "Recurring" else "One-time"
        Toast.makeText(this, "Income Added: Amount = $amount, Category = $category, $recurringStatus", Toast.LENGTH_LONG).show()

        // Optionally, clear fields after adding the income
        amountEditText.text.clear()
        categorySpinner.setSelection(0)
        recurringCheckBox.isChecked = false
    }
}