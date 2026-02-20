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
import com.example.financialtracker.data.model.Income
import com.example.financialtracker.data.repositories.IncomeRepository
import com.google.firebase.Timestamp
import java.util.Calendar

class EditIncomeActivity : BaseActivity() {

    override val navMenuItemId = 0
    private lateinit var amountEditText: EditText
    private lateinit var sourceSpinner: Spinner
    private lateinit var recurringCheckBox: CheckBox
    private lateinit var editIncomeButton: Button
    private lateinit var editDateButton: Button
    private val incomeRepository = IncomeRepository()
    private var selectedDate: Calendar = Calendar.getInstance()
    private var incomeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.edit_income, findViewById(R.id.content_container), true)

        amountEditText = findViewById(R.id.editTextNumber2)
        sourceSpinner = findViewById(R.id.spinner2)
        recurringCheckBox = findViewById(R.id.checkBox2)
        editIncomeButton = findViewById(R.id.editIncomeButton)
        editDateButton = findViewById(R.id.editDateButton)

        ArrayAdapter.createFromResource(
            this,
            R.array.income_categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sourceSpinner.adapter = adapter
        }

        incomeId = intent.getStringExtra("incomeId")
        if (incomeId == null) {
            Toast.makeText(this, "Income ID is missing", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        loadIncomeData(incomeId!!)

        recurringCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showDatePickerDialog()
            }
        }

        editDateButton.setOnClickListener {
            showDatePickerDialog()
        }

        editIncomeButton.setOnClickListener {
            onEditIncomeClicked()
        }
    }

    private fun loadIncomeData(id: String) {
        incomeRepository.getIncomeById(id,
            onSuccess = { income ->
                amountEditText.setText(income.amount.toString())
                val sources = resources.getStringArray(R.array.income_categories)
                val sourcePosition = sources.indexOf(income.source)
                if (sourcePosition >= 0) {
                    sourceSpinner.setSelection(sourcePosition)
                }
                recurringCheckBox.isChecked = income.isRecurring
                if (income.isRecurring && income.recurringDate != null) {
                    selectedDate.time = income.recurringDate.toDate()
                } else {
                    selectedDate.time = income.date?.toDate()
                }
            },
            onFailure = { exception ->
                Toast.makeText(this, "Failed to load income: ${exception.message}", Toast.LENGTH_LONG).show()
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

    private fun onEditIncomeClicked() {
        val amount = amountEditText.text.toString()
        val source = sourceSpinner.selectedItem.toString()
        val isRecurring = recurringCheckBox.isChecked

        if (amount.isEmpty()) {
            Toast.makeText(this, "Amount is required", Toast.LENGTH_SHORT).show()
        } else {
            try {
                val amountValue = amount.toDouble()
                if (amountValue <= 0) {
                    Toast.makeText(this, "Amount must be greater than zero", Toast.LENGTH_SHORT).show()
                } else {
                    updateIncome(incomeId!!, amountValue, source, isRecurring)
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid amount entered", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateIncome(id: String, amount: Double, source: String, isRecurring: Boolean) {
        val recurringTimestamp = if (isRecurring) Timestamp(selectedDate.time) else null
        val income = Income(id = id, amount = amount, source = source, isRecurring = isRecurring, date = Timestamp(selectedDate.time), recurringDate = recurringTimestamp)
        incomeRepository.updateIncome(income, {
            Toast.makeText(this, "Income updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        }, {
            Toast.makeText(this, "Failed to update income: ${it.message}", Toast.LENGTH_LONG).show()
        })
    }
}