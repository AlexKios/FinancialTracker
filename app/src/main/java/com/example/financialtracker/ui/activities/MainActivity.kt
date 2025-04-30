package com.example.financialtracker.ui.activities

import android.os.Bundle
import android.widget.TableRow
import android.widget.TextView
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.ProgressBar
import androidx.core.view.setPadding
import com.example.financialtracker.R

class MainActivity : BaseActivity() {

    override val navMenuItemId = R.id.nav_home
    private lateinit var tableLayout: TableLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewRemaining: TextView
    private lateinit var textViewAmount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_main, findViewById(R.id.content_container), true)

        tableLayout = findViewById(R.id.tableLayout)
        progressBar = findViewById(R.id.progressBar2)
        textViewRemaining = findViewById(R.id.textView2)
        textViewAmount = findViewById(R.id.textView)

        textViewAmount.text = "800лв"
        textViewRemaining.text = getString(R.string.remaining)

        progressBar.progress = 50

        addSampleData()
    }

    private fun addSampleData() {
        val sampleData = listOf(
            ExpenseItem("Groceries", "$50", "18 Mar 2025", R.drawable.home),
            ExpenseItem("Transport", "$15", "17 Mar 2025", R.drawable.home),
            ExpenseItem("Entertainment", "$30", "16 Mar 2025", R.drawable.home),
            ExpenseItem("Dining", "$25", "15 Mar 2025", R.drawable.home),
            ExpenseItem("Shopping", "$100", "14 Mar 2025", R.drawable.home),
            ExpenseItem("Gym Membership", "$40", "13 Mar 2025", R.drawable.home),
            ExpenseItem("Travel", "$200", "12 Mar 2025", R.drawable.home),
            ExpenseItem("Subscription", "$10", "11 Mar 2025", R.drawable.home),
            ExpenseItem("Education", "$60", "10 Mar 2025", R.drawable.home),
            ExpenseItem("Healthcare", "$75", "9 Mar 2025", R.drawable.home)
        )

        for (expense in sampleData) {
            val row = TableRow(this)
            row.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 80)

            val imageView = ImageView(this)
            imageView.layoutParams = TableRow.LayoutParams(50, 50)
            imageView.setImageResource(expense.icon)

            val expenseName = TextView(this)
            expenseName.setPadding(15)
            expenseName.text = expense.name

            val expenseAmount = TextView(this)
            expenseAmount.setPadding(15)
            expenseAmount.text = expense.amount

            val expenseDate = TextView(this)
            expenseDate.setPadding(15)
            expenseDate.text = expense.date

            row.addView(imageView)
            row.addView(expenseName)
            row.addView(expenseAmount)
            row.addView(expenseDate)

            tableLayout.addView(row)
        }
    }

    data class ExpenseItem(val name: String, val amount: String, val date: String, val icon: Int)
}