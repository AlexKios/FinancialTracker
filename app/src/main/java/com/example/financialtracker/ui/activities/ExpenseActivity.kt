package com.example.financialtracker.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financialtracker.R
import com.example.financialtracker.data.model.Expense
import com.example.financialtracker.ui.adapter.ExpenseAdapter
import com.example.financialtracker.ui.viewmodels.ExpenseViewModel

class ExpenseActivity : BaseActivity() {

    override val navMenuItemId = R.id.nav_expense
    private lateinit var viewModel: ExpenseViewModel
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.expenses, findViewById(R.id.content_container), true)

        val button = findViewById<Button>(R.id.buttonAddExpense)
        button.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.expensesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ExpenseAdapter(emptyList()) { expense ->
            showEditDeleteDialog(expense)
        }
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[ExpenseViewModel::class.java]
        viewModel.expenses.observe(this) { expenses ->
            adapter.updateData(expenses)
        }
    }

    private fun showEditDeleteDialog(expense: Expense) {
        val options = arrayOf("Edit", "Delete")

        AlertDialog.Builder(this)
            .setTitle("Choose an option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, EditExpenseActivity::class.java)
                        intent.putExtra("expenseId", expense.id)
                        startActivity(intent)
                    }
                    1 -> {
                        viewModel.deleteExpense(expense.id)
                    }
                }
            }
            .show()
    }
}