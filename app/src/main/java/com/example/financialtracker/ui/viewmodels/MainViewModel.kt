package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financialtracker.data.repositories.ExpenseRepository
import com.example.financialtracker.data.repositories.IncomeRepository
import com.example.financialtracker.data.repositories.UserRepository
import java.text.SimpleDateFormat
import java.util.Locale


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
                listenForTransactions()
            },
            onFailure = {
            }
        )
    }

    private fun listenForTransactions() {
        incomeRepo.listenForIncomes(
            onSuccess = { incomes ->
                expenseRepo.listenForExpenses(
                    onSuccess = { expenses ->
                        val transactions = mutableListOf<Triple<String, Double, String>>()
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                        incomes.forEach { income ->
                            val dateStr = formatter.format(income.date!!.toDate())
                            transactions.add(Triple("Income: ${income.source}", income.amount, dateStr))
                        }

                        expenses.forEach { expense ->
                            val dateStr = formatter.format(expense.date!!.toDate())
                            transactions.add(Triple("Expense: ${expense.category}", expense.amount, dateStr))
                        }

                        _allTransactions.value = transactions
                    },
                    onFailure = { 
                    }
                )
            },
            onFailure = { 
            }
        )
    }

    fun calculateBudgetPercentage(remaining: Double): Int {
        return ((remaining / 1000) * 100).toInt().coerceIn(0, 100)
    }

    override fun onCleared() {
        super.onCleared()
        incomeRepo.unregisterListener()
        expenseRepo.unregisterListener()
    }
}