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

class AddExpenseActivity : AppCompatActivity() {

    // Declare the UI elements
    private lateinit var amountEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var recurringCheckBox: CheckBox
    private lateinit var addExpenseButton: Button
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_expense) // Replace with your actual XML layout file name

        // Initialize UI elements
        amountEditText = findViewById(R.id.editTextNumber)
        categorySpinner = findViewById(R.id.spinner)
        recurringCheckBox = findViewById(R.id.checkBox)
        addExpenseButton = findViewById(R.id.button2)
        toolbar = findViewById(R.id.toolbar2)

        // Set up the toolbar (if needed for navigation, such as a back button)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set click listener for the Add Expense button
        addExpenseButton.setOnClickListener {
            onAddExpenseClicked()
        }
    }

    // Handle the Add Expense button click
    private fun onAddExpenseClicked() {
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
                    // Logic for adding the expense (e.g., save to database or API)
                    saveExpense(amountValue, category, isRecurring)
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid amount entered", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Save the expense (you can replace this with your actual saving logic, like database or API)
    private fun saveExpense(amount: Double, category: String, isRecurring: Boolean) {
        // For now, just show a Toast message with the details
        val recurringStatus = if (isRecurring) "Recurring" else "One-time"
        Toast.makeText(this, "Expense Added: Amount = $amount, Category = $category, $recurringStatus", Toast.LENGTH_LONG).show()

        // Optionally, clear fields after adding the expense
        amountEditText.text.clear()
        categorySpinner.setSelection(0)
        recurringCheckBox.isChecked = false
    }
}