package com.example.financialtracker.ui.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.example.financialtracker.R
import com.example.financialtracker.data.model.Expense
import com.example.financialtracker.data.repositories.ExpenseRepository
import com.google.firebase.Timestamp
import java.util.Calendar

class AddExpenseActivity : BaseActivity() {

    override val navMenuItemId = 0
    // Declare the UI elements
    private lateinit var amountEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var recurringCheckBox: CheckBox
    private lateinit var addExpenseButton: Button
    private val expenseRepository = ExpenseRepository()
    private var selectedDate: Calendar = Calendar.getInstance()
    private var expenseId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.add_expense, findViewById(R.id.content_container), true)

        // Initialize UI elements
        amountEditText = findViewById(R.id.editTextNumber)
        categorySpinner = findViewById(R.id.spinner)
        recurringCheckBox = findViewById(R.id.checkBox)
        addExpenseButton = findViewById(R.id.button2)

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.expense_categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            categorySpinner.adapter = adapter
        }

        expenseId = intent.getStringExtra("expenseId")
        if (expenseId != null) {
            addExpenseButton.text = getString(R.string.update_expense)
            loadExpenseData(expenseId!!)
        }


        recurringCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showDatePickerDialog()
            }
        }

        // Set click listener for the Add Expense button
        addExpenseButton.setOnClickListener {
            onAddExpenseClicked()
        }
    }

    private fun loadExpenseData(id: String) {
        expenseRepository.getExpenseById(id,
            onSuccess = { expense ->
                amountEditText.setText(expense.amount.toString())
                val categories = resources.getStringArray(R.array.expense_categories)
                val categoryPosition = categories.indexOf(expense.category)
                if (categoryPosition >= 0) {
                    categorySpinner.setSelection(categoryPosition)
                }
                recurringCheckBox.isChecked = expense.isRecurring
                if (expense.isRecurring && expense.recurringDate != null) {
                    selectedDate.time = expense.recurringDate.toDate()
                }
            },
            onFailure = { exception ->
                Toast.makeText(this, "Failed to load expense: ${exception.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        )
    }


    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate.set(selectedYear, selectedMonth, selectedDay)
        }, year, month, day)
        datePickerDialog.show()
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
                    if (expenseId == null) {
                        saveExpense(amountValue, category, isRecurring)
                    } else {
                        updateExpense(expenseId!!, amountValue, category, isRecurring)
                    }
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid amount entered", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateExpense(id: String, amount: Double, category: String, isRecurring: Boolean) {
        val recurringTimestamp = if (isRecurring) Timestamp(selectedDate.time) else null
        val expense = Expense(id = id, amount = amount, category = category, isRecurring = isRecurring, date = Timestamp.now(), recurringDate = recurringTimestamp)
        expenseRepository.updateExpense(expense, {
            Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        }, {
            Toast.makeText(this, "Failed to update expense: ${it.message}", Toast.LENGTH_LONG).show()
        })
    }

    // Save the expense (you can replace this with your actual saving logic, like database or API)
    private fun saveExpense(amount: Double, category: String, isRecurring: Boolean) {
        val recurringTimestamp = if (isRecurring) Timestamp(selectedDate.time) else null
        val expense = Expense(amount = amount, category = category, isRecurring = isRecurring, date = Timestamp.now(), recurringDate = recurringTimestamp)
        expenseRepository.addExpense(expense, {
            Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show()
            finish()
        }, {
            Toast.makeText(this, "Failed to add expense: ${it.message}", Toast.LENGTH_LONG).show()
        })
    }
}