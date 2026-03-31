package com.example.financialtracker.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financialtracker.R
import com.example.financialtracker.data.adapter.IncomeAdapter
import com.example.financialtracker.data.model.Income
import com.example.financialtracker.ui.viewmodels.IncomeViewModel

class IncomeActivity : BaseActivity() {

    override val navMenuItemId = R.id.nav_income
    private lateinit var viewModel: IncomeViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: IncomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.income, findViewById(R.id.content_container), true)

        initViews()
        setupViewModel()
    }

    private fun initViews() {
        val addButton = findViewById<Button>(R.id.button5)
        addButton.setOnClickListener {
            startActivity(Intent(this, AddIncomeActivity::class.java))
        }

        recyclerView = findViewById(R.id.incomeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = IncomeAdapter(emptyList()) { income ->
            showEditDeleteDialog(income)
        }
        recyclerView.adapter = adapter
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[IncomeViewModel::class.java]

        viewModel.incomes.observe(this) { incomes ->
            adapter.updateIncomes(incomes)
        }
    }

    private fun showEditDeleteDialog(income: Income) {
        val options = arrayOf("Edit", "Delete")

        AlertDialog.Builder(this)
            .setTitle("Choose an option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, EditIncomeActivity::class.java).apply {
                            putExtra("incomeId", income.id)
                        }
                        startActivity(intent)
                    }
                    1 -> {
                        viewModel.deleteIncome(income.id)
                    }
                }
            }
            .show()
    }
}
