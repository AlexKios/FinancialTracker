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
import com.example.financialtracker.data.model.Income
import com.example.financialtracker.data.repositories.IncomeRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.util.Calendar

class AddIncomeActivity : BaseActivity() {

    override val navMenuItemId = 0

    private lateinit var amountEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var recurringCheckBox: CheckBox
    private lateinit var addIncomeButton: Button
    private val incomeRepository = IncomeRepository()
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.add_income, findViewById(R.id.content_container), true)

        amountEditText = findViewById(R.id.editTextNumber2)
        categorySpinner = findViewById(R.id.spinner2)
        recurringCheckBox = findViewById(R.id.checkBox2)
        addIncomeButton = findViewById(R.id.button4)

        ArrayAdapter.createFromResource(
            this,
            R.array.income_categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        recurringCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showDatePickerDialog()
            }
        }

        addIncomeButton.setOnClickListener {
            onAddIncomeClicked()
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

    private fun onAddIncomeClicked() {
        val amount = amountEditText.text.toString()
        val source = categorySpinner.selectedItem.toString()
        val isRecurring = recurringCheckBox.isChecked

        if (amount.isEmpty()) {
            Toast.makeText(this, "Amount is required", Toast.LENGTH_SHORT).show()
        } else {
            try {
                val amountValue = amount.toDouble()
                if (amountValue <= 0) {
                    Toast.makeText(this, "Amount must be greater than zero", Toast.LENGTH_SHORT).show()
                } else {
                    saveIncome(amountValue, source, isRecurring)
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid amount entered", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveIncome(amount: Double, source: String, isRecurring: Boolean) {
        val recurringTimestamp = if (isRecurring) Timestamp(selectedDate.time) else null
        val income = Income(
            amount = amount,
            source = source,
            isRecurring = isRecurring,
            date = Timestamp.now(),
            recurringDate = recurringTimestamp
        )

        lifecycleScope.launch {
            try {
                incomeRepository.addIncome(income)
                Toast.makeText(this@AddIncomeActivity, "Income added successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddIncomeActivity, "Failed to add income: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
