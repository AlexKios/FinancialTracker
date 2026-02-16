package com.example.financialtracker.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.financialtracker.R
import com.example.financialtracker.data.model.Expense
import com.example.financialtracker.ui.viewmodels.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseActivity : BaseActivity() {

    override val navMenuItemId = R.id.nav_expense
    private lateinit var viewModel: ExpenseViewModel
    private lateinit var tableLayout: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.expenses, findViewById(R.id.content_container), true)

        val button = findViewById<Button>(R.id.button5)
        button.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }

        tableLayout = findViewById(R.id.tableLayout)
        viewModel = ViewModelProvider(this)[ExpenseViewModel::class.java]

        viewModel.expenses.observe(this) { expenses ->
            populateTable(expenses)
        }
    }

    private fun populateTable(expenses: List<Expense>) {
        // Clear all data rows except the header
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }

        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        for (expense in expenses) {
            val row = TableRow(this).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                minimumHeight = 80
                gravity = Gravity.CENTER_VERTICAL
            }

            val icon = ImageView(this).apply {
                val iconResource = when (expense.category) {
                    "Groceries" -> R.drawable.expenses
                    "Transport" -> R.drawable.expenses
                    "Entertainment" -> R.drawable.expenses
                    "Dining" -> R.drawable.expenses
                    "Shopping" -> R.drawable.expenses
                    "Gym Membership" -> R.drawable.expenses
                    "Travel" -> R.drawable.expenses
                    "Subscription" -> R.drawable.expenses
                    "Education" -> R.drawable.expenses
                    "Healthcare" -> R.drawable.expenses
                    else -> android.R.drawable.ic_menu_info_details // Default icon
                }
                setImageResource(iconResource)
                layoutParams = TableRow.LayoutParams(100, 100)
            }

            val category = TextView(this).apply {
                text = expense.category
                setPadding(15, 10, 15, 10)
                setTextColor(getColor(R.color.textPrimary))
            }

            val amount = TextView(this).apply {
                text = getString(R.string.amount_format, expense.amount)
                setPadding(15, 10, 15, 10)
                setTextColor(getColor(R.color.textPrimary))
            }

            val date = TextView(this).apply {
                text = expense.date?.toDate()?.let { formatter.format(it) } ?: ""
                setPadding(15, 10, 15, 10)
                setTextColor(getColor(R.color.textPrimary))
            }

            row.addView(icon)
            row.addView(category)
            row.addView(amount)
            row.addView(date)

            tableLayout.addView(row)
        }
    }
}