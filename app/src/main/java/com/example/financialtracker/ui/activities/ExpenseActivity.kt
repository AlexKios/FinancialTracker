package com.example.financialtracker.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.example.financialtracker.R

class ExpenseActivity : BaseActivity() {

    data class Expense(
        val iconResId: Int,
        val name: String,
        val amount: String,
        val date: String
    )

    override val navMenuItemId = R.id.nav_expense

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.expenses, findViewById(R.id.content_container), true)

        val button = findViewById<Button>(R.id.button5)
        button.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }

        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

        // Clear the sample rows if needed (keep header only)
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }

        // Temporary sample data (replace with DB fetch later)
        val expenses = listOf(
            Expense(android.R.drawable.ic_menu_gallery, "Groceries", "$50", "18 Mar 2025"),
            Expense(android.R.drawable.ic_menu_camera, "Transport", "$15", "17 Mar 2025"),
            Expense(android.R.drawable.ic_menu_compass, "Entertainment", "$30", "16 Mar 2025"),
            Expense(android.R.drawable.ic_menu_call, "Dining", "$25", "15 Mar 2025"),
            Expense(android.R.drawable.ic_menu_crop, "Shopping", "$100", "14 Mar 2025"),
            Expense(android.R.drawable.ic_menu_agenda, "Gym Membership", "$40", "13 Mar 2025"),
            Expense(android.R.drawable.ic_menu_mapmode, "Travel", "$200", "12 Mar 2025"),
            Expense(android.R.drawable.ic_menu_manage, "Subscription", "$10", "11 Mar 2025"),
            Expense(android.R.drawable.ic_menu_edit, "Education", "$60", "10 Mar 2025"),
            Expense(android.R.drawable.ic_menu_help, "Healthcare", "$75", "9 Mar 2025")
        )

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
                setImageResource(expense.iconResId)
                layoutParams = TableRow.LayoutParams(100, 100)
            }

            val name = TextView(this).apply {
                text = expense.name
                setPadding(15, 10, 15, 10)
                setTextColor(getColor(R.color.textPrimary))
            }

            val amount = TextView(this).apply {
                text = expense.amount
                setPadding(15, 10, 15, 10)
                setTextColor(getColor(R.color.textPrimary))
            }

            val date = TextView(this).apply {
                text = expense.date
                setPadding(15, 10, 15, 10)
                setTextColor(getColor(R.color.textPrimary))
            }

            row.addView(icon)
            row.addView(name)
            row.addView(amount)
            row.addView(date)

            tableLayout.addView(row)
        }
    }
}