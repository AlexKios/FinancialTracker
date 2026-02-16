package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financialtracker.data.model.User
import com.example.financialtracker.data.repositories.ExpenseRepository
import com.example.financialtracker.data.repositories.IncomeRepository
import com.example.financialtracker.data.repositories.UserRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MainViewModel : ViewModel() {

    private val userRepo = UserRepository()
    private val incomeRepo = IncomeRepository()
    private val expenseRepo = ExpenseRepository()

    private var currentUser: User? = null

    private val _budget = MutableLiveData<Double>()
    val budget: LiveData<Double> = _budget

    private val _allTransactions = MutableLiveData<List<Triple<String, Double, String>>>()
    val allTransactions: LiveData<List<Triple<String, Double, String>>> get() = _allTransactions

    fun loadUserData() {
        userRepo.getCurrentUser(
            onSuccess = { user ->
                currentUser = user
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
                            income.date?.let { timestamp ->
                                val dateStr = formatter.format(timestamp.toDate())
                                transactions.add(Triple("Income: ${income.source}", income.amount, dateStr))
                            }
                        }

                        expenses.forEach { expense ->
                            expense.date?.let { timestamp ->
                                val dateStr = formatter.format(timestamp.toDate())
                                transactions.add(Triple("Expense: ${expense.category}", expense.amount, dateStr))
                            }
                        }

                        _allTransactions.value = transactions

                        val calendar = Calendar.getInstance()
                        val currentMonth = calendar.get(Calendar.MONTH)
                        val currentYear = calendar.get(Calendar.YEAR)

                        val monthlyExpenses = expenses.filter { expense ->
                            val expenseCalendar = Calendar.getInstance()
                            expense.date?.let { timestamp ->
                                expenseCalendar.time = timestamp.toDate()
                                expenseCalendar.get(Calendar.MONTH) == currentMonth &&
                                expenseCalendar.get(Calendar.YEAR) == currentYear
                            } ?: false
                        }

                        val totalMonthlyExpenses = monthlyExpenses.sumOf { it.amount }

                        val totalBudget = currentUser?.budget ?: 0.0
                        _budget.value = totalBudget - totalMonthlyExpenses
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
        val totalBudget = currentUser?.budget ?: 1.0
        if (totalBudget == 0.0) return 0
        return ((remaining / totalBudget) * 100).toInt().coerceIn(0, 100)
    }

    fun setBudget(budget: Double) {
        userRepo.updateBudget(budget, 
            onSuccess = { 
                currentUser?.budget = budget
                listenForTransactions() // Recalculate
            },
            onFailure = {
                // Handle failure
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        incomeRepo.unregisterListener()
        expenseRepo.unregisterListener()
    }
}