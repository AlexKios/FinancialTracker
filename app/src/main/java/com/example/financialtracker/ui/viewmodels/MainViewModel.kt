package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financialtracker.data.repositories.ExpenseRepository
import com.example.financialtracker.data.repositories.IncomeRepository
import com.example.financialtracker.data.repositories.UserRepository



class MainViewModel : ViewModel() {

    private val userRepo = UserRepository()
    private val incomeRepo = IncomeRepository()
    private val expenseRepo = ExpenseRepository()

    private val _budget = MutableLiveData<Double>()
    val budget: LiveData<Double> = _budget

    private val _allTransactions = MutableLiveData<List<Triple<String, Double, String>>>()
    val allTransactions: LiveData<List<Triple<String, Double, String>>> get() = _allTransactions

    fun loadUserData() {
        userRepo.getCurrentUser(
            onSuccess = { user ->
                _budget.value = user.budget
                loadTransactions(user.incomeIds, user.expenseIds)
            },
            onFailure = {
            }
        )
    }

    private fun loadTransactions(incomeIds: List<String>, expenseIds: List<String>) {
        val transactions = mutableListOf<Triple<String, Double, String>>()

        if (incomeIds.isEmpty() && expenseIds.isEmpty()) {
            _allTransactions.value = emptyList()
            return
        }

        var remainingFetches = incomeIds.size + expenseIds.size
        if (remainingFetches == 0) {
            _allTransactions.value = transactions
            return
        }

        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

        incomeIds.forEach { id ->
            incomeRepo.getIncomeById(id,
                onSuccess = { income ->
                    val dateStr = formatter.format(income.date!!.toDate())
                    transactions.add(Triple("Income: ${income.source}", income.amount, dateStr))
                    if (--remainingFetches == 0) _allTransactions.value = transactions
                },
                onFailure = {
                    if (--remainingFetches == 0) _allTransactions.value = transactions
                }
            )
        }

        expenseIds.forEach { id ->
            expenseRepo.getExpenseById(id,
                onSuccess = { expense ->
                    val dateStr = formatter.format(expense.date!!.toDate())
                    transactions.add(Triple("Expense: ${expense.category}", expense.amount, dateStr))
                    if (--remainingFetches == 0) _allTransactions.value = transactions
                },
                onFailure = {
                    if (--remainingFetches == 0) _allTransactions.value = transactions
                }
            )
        }
    }

    fun calculateBudgetPercentage(remaining: Double): Int {
        return ((remaining / 1000) * 100).toInt().coerceIn(0, 100)
    }
}