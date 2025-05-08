package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financialtracker.data.repositories.ExpenseRepository
import com.example.financialtracker.data.repositories.IncomeRepository
import com.example.financialtracker.data.repositories.UserRepository

private val userRepo = UserRepository()
private val incomeRepo = IncomeRepository()
private val expenseRepo = ExpenseRepository()

private val _budget = MutableLiveData<Double>()
val budget: LiveData<Double> = _budget

private val _allTransactions = MutableLiveData<List<Pair<String, Double>>>()
val allTransactions: LiveData<List<Pair<String, Double>>> = _allTransactions

class MainViewModel : ViewModel() {
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
        val transactions = mutableListOf<Pair<String, Double>>()

        if (incomeIds.isEmpty() && expenseIds.isEmpty()) {
            _allTransactions.value = emptyList()
            return
        }

        var remainingFetches = incomeIds.size + expenseIds.size
        if (remainingFetches == 0) {
            _allTransactions.value = transactions
            return
        }

        incomeIds.forEach { id ->
            incomeRepo.getIncomeById(id,
                onSuccess = { income ->
                    transactions.add("Income" to income.amount)
                    if (--remainingFetches == 0) _allTransactions.value = transactions
                },
                onFailure = { if (--remainingFetches == 0) _allTransactions.value = transactions }
            )
        }

        expenseIds.forEach { id ->
            expenseRepo.getExpenseById(id,
                onSuccess = { expense ->
                    transactions.add("Expense" to expense.amount)
                    if (--remainingFetches == 0) _allTransactions.value = transactions
                },
                onFailure = { if (--remainingFetches == 0) _allTransactions.value = transactions }
            )
        }
    }

    fun calculateBudgetPercentage(remaining: Double): Int {
        return ((remaining / 1000) * 100).toInt().coerceIn(0, 100)
    }
}