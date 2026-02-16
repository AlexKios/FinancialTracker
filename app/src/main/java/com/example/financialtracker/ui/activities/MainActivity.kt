package com.example.financialtracker.ui.activities

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TableRow
import android.widget.TextView
import android.widget.TableLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.financialtracker.R
import com.example.financialtracker.ui.viewmodels.MainViewModel
import java.util.Locale

class MainActivity : BaseActivity() {

    override val navMenuItemId = R.id.nav_home
    private lateinit var tableLayout: TableLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewAmount: TextView
    private lateinit var viewModel: MainViewModel
    private lateinit var setBudgetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_main, findViewById(R.id.content_container), true)

        tableLayout = findViewById(R.id.tableLayout)
        progressBar = findViewById(R.id.progressBudget)
        textViewAmount = findViewById(R.id.remainingBudget)
        setBudgetButton = findViewById(R.id.setBudgetButton)

        textViewAmount.text = ""

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        observeViewModel()
        viewModel.loadUserData()

        setBudgetButton.setOnClickListener {
            showSetBudgetDialog()
        }
    }

    private fun showSetBudgetDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_set_budget, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextBudget)

        builder.setView(dialogLayout)
        builder.setPositiveButton("OK") { _, _ ->
            val budgetString = editText.text.toString()
            if (budgetString.isNotEmpty()) {
                val budget = budgetString.toDouble()
                viewModel.setBudget(budget)
            }
        }
        builder.setNegativeButton("Cancel") { _, _ ->
        }
        builder.show()
    }

    private fun observeViewModel() {
        viewModel.budget.observe(this) { remaining ->
            textViewAmount.text = String.format(Locale.US, "$%.2f", remaining)
            val percentage = viewModel.calculateBudgetPercentage(remaining)
            updateProgressBar(remaining, percentage)
        }

        viewModel.allTransactions.observe(this) { transactions ->
            populateTable(transactions)
        }
    }

    private fun updateProgressBar(remaining: Double, percentage: Int) {
        val progressDrawable = progressBar.progressDrawable.mutate() as LayerDrawable
        val progressLayer = progressDrawable.findDrawableByLayerId(android.R.id.progress)

        if (remaining < 0) {
            progressBar.progress = progressBar.max
            progressLayer.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark), PorterDuff.Mode.SRC_IN)
        } else {
            progressBar.progress = percentage
            val green = (2.55 * percentage).toInt()
            val red = 255 - green
            progressLayer.colorFilter = PorterDuffColorFilter(Color.rgb(red, green, 0), PorterDuff.Mode.SRC_IN)
        }
    }

    private fun populateTable(transactions: List<Triple<String, Double, String>>) {
        val header = tableLayout.getChildAt(0)
        tableLayout.removeAllViews()
        tableLayout.addView(header)

        transactions.forEach { (category, amount, date) ->
            val row = LayoutInflater.from(this).inflate(R.layout.table_row_layout, tableLayout, false) as TableRow

            val iconTextView = row.findViewById<TextView>(R.id.column_icon)
            val typeTextView = row.findViewById<TextView>(R.id.column_type)
            val amountTextView = row.findViewById<TextView>(R.id.column_amount)
            val dateTextView = row.findViewById<TextView>(R.id.column_date)

            if (category.startsWith("Income")) {
                iconTextView.text = "💰"
            } else {
                iconTextView.text = "💸"
            }

            typeTextView.text = category
            amountTextView.text = String.format(Locale.US, "$%.2f", amount)
            dateTextView.text = date

            tableLayout.addView(row)
        }
    }
}
