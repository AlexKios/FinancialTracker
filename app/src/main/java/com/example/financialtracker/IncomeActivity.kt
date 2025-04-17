package com.example.financialtracker

import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IncomeActivity : AppCompatActivity() {

    data class Income(
        val iconResId: Int,
        val source: String,
        val amount: String,
        val date: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.income) // <- Make sure this matches your layout file name

        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

        // Clear all data rows except the header
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }

        // Temporary mock data (replace with database retrieval later)
        val incomes = listOf(
            Income(android.R.drawable.ic_menu_info_details, "Salary", "$2000", "25 Mar 2025"),
            Income(android.R.drawable.ic_menu_myplaces, "Freelance", "$500", "20 Mar 2025"),
            Income(android.R.drawable.ic_menu_gallery, "Investments", "$300", "18 Mar 2025"),
            Income(android.R.drawable.ic_menu_camera, "Side Hustle", "$150", "15 Mar 2025"),
            Income(android.R.drawable.ic_menu_agenda, "Rental Income", "$800", "10 Mar 2025"),
            Income(android.R.drawable.ic_menu_mapmode, "Bonus", "$1000", "5 Mar 2025")
        )

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
                setImageResource(income.iconResId)
                layoutParams = TableRow.LayoutParams(100, 100)
            }

            val source = TextView(this).apply {
                text = income.source
                setPadding(15, 10, 15, 10)
                setTextColor(getColor(R.color.textPrimary))
            }

            val amount = TextView(this).apply {
                text = income.amount
                setPadding(15, 10, 15, 10)
                setTextColor(getColor(R.color.textPrimary))
            }

            val date = TextView(this).apply {
                text = income.date
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