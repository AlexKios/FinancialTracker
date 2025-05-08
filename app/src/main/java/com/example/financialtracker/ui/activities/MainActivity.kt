package com.example.financialtracker.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TableRow
import android.widget.TextView
import android.widget.TableLayout
import android.widget.ProgressBar
import androidx.activity.viewModels
import com.example.financialtracker.R
import com.example.financialtracker.ui.viewmodels.MainViewModel

class MainActivity : BaseActivity() {

    override val navMenuItemId = R.id.nav_home
    private lateinit var tableLayout: TableLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewAmount: TextView
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_main, findViewById(R.id.content_container), true)

        tableLayout = findViewById(R.id.tableLayout)
        progressBar = findViewById(R.id.progressBudget)
        textViewAmount = findViewById(R.id.remainingBudget)

        textViewAmount.text = ""

        progressBar.progress = 50

        observeViewModel()
        viewModel.loadUserData()

    }

    private fun observeViewModel() {
        viewModel.budget.observe(this) { remaining ->
            textViewAmount.text = "Remaining: $%.2f".format(remaining)
            progressBar.progress = viewModel.calculateBudgetPercentage(remaining)
        }

        viewModel.load.observe(this) { transactions ->
            populateTable(transactions)
        }
    }

    private fun populateTable(transactions: List<Triple<String, Double, String>>) {
        tableLayout.removeAllViews()

        transactions.forEach { (type, amount, dateStr) ->
            val row = LayoutInflater.from(this).inflate(R.layout.table_row_layout, null) as TableRow

            val typeTextView = row.findViewById<TextView>(R.id.column_type)
            val amountTextView = row.findViewById<TextView>(R.id.column_amount)
            val dateTextView = row.findViewById<TextView>(R.id.column_date)
            val iconTextView = row.findViewById<TextView>(R.id.column_icon)

            typeTextView.text = type
            amountTextView.text = "$%.2f".format(amount)
            dateTextView.text = dateStr
            iconTextView.text = if (type.lowercase().contains("income")) "💰" else "💸"

            tableLayout.addView(row)
        }
    }

}