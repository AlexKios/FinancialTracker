package com.example.financialtracker.ui.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.financialtracker.R
import com.example.financialtracker.data.model.Expense
import com.example.financialtracker.data.repositories.ExpenseRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.util.Calendar

class EditExpenseActivity : BaseActivity() {

    override val navMenuItemId = 0

    private lateinit var amountEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var recurringCheckBox: CheckBox
    private lateinit var editExpenseButton: Button
    private lateinit var editDateButton: Button
    private val expenseRepository = ExpenseRepository()
    private var selectedDate: Calendar = Calendar.getInstance()
    private var expenseId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.edit_expense, findViewById(R.id.content_container), true)

        amountEditText = findViewById(R.id.editTextNumber)
        categorySpinner = findViewById(R.id.spinner)
        recurringCheckBox = findViewById(R.id.checkBox)
        editExpenseButton = findViewById(R.id.editExpenseButton)
        editDateButton = findViewById(R.id.editDateButton)

        ArrayAdapter.createFromResource(
            this,
            R.array.expense_categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        expenseId = intent.getStringExtra("expenseId")
        if (expenseId == null) {
            Toast.makeText(this, "Expense ID is missing", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        loadExpenseData(expenseId!!)

        recurringCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showDatePickerDialog()
            }
        }

        editDateButton.setOnClickListener {
            showDatePickerDialog()
        }

        editExpenseButton.setOnClickListener {
            onEditExpenseClicked()
        }
    }

    private fun loadExpenseData(id: String) {
        lifecycleScope.launch {
            try {
                val expense = expenseRepository.getExpenseById(id)
                amountEditText.setText(expense.amount.toString())
                val categories = resources.getStringArray(R.array.expense_categories)
                val categoryPosition = categories.indexOf(expense.category)
                if (categoryPosition >= 0) {
                    categorySpinner.setSelection(categoryPosition)
                }
                recurringCheckBox.isChecked = expense.isRecurring
                if (expense.isRecurring && expense.recurringDate != null) {
                    selectedDate.time = expense.recurringDate.toDate()
                } else {
                    selectedDate.time = expense.date?.toDate() ?: Calendar.getInstance().time
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditExpenseActivity, "Failed to load expense: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
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

    private fun onEditExpenseClicked() {
        val amount = amountEditText.text.toString()
        val category = categorySpinner.selectedItem.toString()
        val isRecurring = recurringCheckBox.isChecked

        if (amount.isEmpty()) {
            Toast.makeText(this, "Amount is required", Toast.LENGTH_SHORT).show()
        } else {
            try {
                val amountValue = amount.toDouble()
                if (amountValue <= 0) {
                    Toast.makeText(this, "Amount must be greater than zero", Toast.LENGTH_SHORT).show()
                } else {
                    updateExpenseRecord(expenseId!!, amountValue, category, isRecurring)
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid amount entered", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateExpenseRecord(id: String, amount: Double, category: String, isRecurring: Boolean) {
        val recurringTimestamp = if (isRecurring) Timestamp(selectedDate.time) else null
        val expense = Expense(
            id = id,
            amount = amount,
            category = category,
            isRecurring = isRecurring,
            date = Timestamp(selectedDate.time),
            recurringDate = recurringTimestamp
        )
        
        lifecycleScope.launch {
            try {
                expenseRepository.updateExpense(expense)
                Toast.makeText(this@EditExpenseActivity, "Expense updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@EditExpenseActivity, "Failed to update expense: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
