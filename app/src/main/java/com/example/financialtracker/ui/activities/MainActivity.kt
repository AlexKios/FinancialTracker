package com.example.financialtracker.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TableRow
import android.widget.TextView
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.example.financialtracker.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewRemaining: TextView
    private lateinit var textViewAmount: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Views
        tableLayout = findViewById(R.id.tableLayout)
        progressBar = findViewById(R.id.progressBar2)
        textViewRemaining = findViewById(R.id.textView2)
        textViewAmount = findViewById(R.id.textView)

        // Set sample remaining amount
        textViewAmount.text = "800лв"
        textViewRemaining.text = getString(R.string.remaining)

        // Set sample progress
        progressBar.progress = 50 // Example: 50% progress

        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView2)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_income -> {
                    // Start the income activity
                    val intent = Intent(this, IncomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_home -> {
                    // Start the home activity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_expense -> {
                    // Start the expense activity
                    val intent = Intent(this, ExpenseActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar2)
        setSupportActionBar(toolbar)

        // 2) Handle navigation (user icon) click
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, AccountActivity::class.java))
        }

        // Add sample data to TableLayout
        addSampleData()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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