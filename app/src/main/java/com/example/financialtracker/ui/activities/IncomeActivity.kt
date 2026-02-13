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
import com.example.financialtracker.data.model.Income
import com.example.financialtracker.ui.viewmodels.IncomeViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class IncomeActivity : BaseActivity() {

    override val navMenuItemId = R.id.nav_income
    private lateinit var viewModel: IncomeViewModel
    private lateinit var tableLayout: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.income, findViewById(R.id.content_container), true)

        val button = findViewById<Button>(R.id.button5)
        button.setOnClickListener {
            val intent = Intent(this, AddIncomeActivity::class.java)
            startActivity(intent)
        }

        tableLayout = findViewById(R.id.tableLayout)
        viewModel = ViewModelProvider(this)[IncomeViewModel::class.java]

        viewModel.incomes.observe(this) { incomes ->
            populateTable(incomes)
        }
    }

    private fun populateTable(incomes: List<Income>) {
        // Clear all data rows except the header
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }

        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        for (income in incomes) {
            val row = TableRow(this).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                minimumHeight = 80
                gravity = Gravity.CENTER_VERTICAL
            }

            val icon = ImageView(this).apply {
                val iconResource = when (income.source) {
                    "Salary" -> R.drawable.profit
                    "Freelance" -> R.drawable.id_card
                    "Investment" -> R.drawable.profit
                    "Gift" -> R.drawable.profit
                    else -> android.R.drawable.ic_menu_info_details // Default icon
                }
                setImageResource(iconResource)
                layoutParams = TableRow.LayoutParams(100, 100)
            }

            val source = TextView(this).apply {
                text = income.source
                setPadding(15, 10, 15, 10)
                setTextColor(getColor(R.color.textPrimary))
            }

            val amount = TextView(this).apply {
                text = getString(R.string.amount_format, income.amount)
                setPadding(15, 10, 15, 10)
                setTextColor(getColor(R.color.textPrimary))
            }

            val date = TextView(this).apply {
                text = income.date?.toDate()?.let { formatter.format(it) } ?: ""
                setPadding(15, 10, 15, 10)
                setTextColor(getColor(R.color.textPrimary))
            }

            row.addView(icon)
            row.addView(source)
            row.addView(amount)
            row.addView(date)

            tableLayout.addView(row)
        }
    }
}