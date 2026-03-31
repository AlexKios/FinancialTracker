package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financialtracker.data.model.Expense
import com.example.financialtracker.data.repositories.ExpenseRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ExpenseViewModel : ViewModel() {

    private val expenseRepository = ExpenseRepository()

    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List<Expense>> = _expenses

    init {
        listenForExpenses()
    }

    private fun listenForExpenses() {
        viewModelScope.launch {
            expenseRepository.listenForExpenses()
                .catch { /* Handle error */ }
                .collect { expenses ->
                    _expenses.value = expenses
                }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                expenseRepository.deleteExpense(expenseId)
            } catch (e: Exception) {
            }
        }
    }
}
