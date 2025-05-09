package com.example.financialtracker.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TableRow
import android.widget.TextView
import android.widget.TableLayout
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import com.example.financialtracker.R
import com.example.financialtracker.ui.viewmodels.MainViewModel

class MainActivity : BaseActivity() {

    override val navMenuItemId = R.id.nav_home
    private lateinit var tableLayout: TableLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewAmount: TextView
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_main, findViewById(R.id.content_container), true)

        tableLayout = findViewById(R.id.tableLayout)
        progressBar = findViewById(R.id.progressBudget)
        textViewAmount = findViewById(R.id.remainingBudget)

        textViewAmount.text = ""

        progressBar.progress = 50

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        observeViewModel()
        viewModel.loadUserData()

    }

    private fun observeViewModel() {
        viewModel.budget.observe(this) { remaining ->
            textViewAmount.text = "".format(remaining)
            progressBar.progress = viewModel.calculateBudgetPercentage(remaining)
        }

        viewModel.allTransactions.observe(this) { transactions ->
            populateTable(transactions)
        }
    }

    private fun populateTable(transactions: List<Triple<String, Double, String>>) {
        tableLayout.removeAllViews()

        transactions.forEach { (category, amount, date) ->
            val row = LayoutInflater.from(this).inflate(R.layout.table_row_layout, null) as TableRow

            val categoryTextView = row.findViewById<TextView>(R.id.column_icon)
            val amountTextView = row.findViewById<TextView>(R.id.column_amount)
            val dateTextView = row.findViewById<TextView>(R.id.column_date)

            categoryTextView.text = category
            amountTextView.text = "".format(amount)
            dateTextView.text = date

            tableLayout.addView(row)
        }
    }
}