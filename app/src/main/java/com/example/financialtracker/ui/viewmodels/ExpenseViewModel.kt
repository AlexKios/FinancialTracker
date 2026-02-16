package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financialtracker.data.model.Expense
import com.example.financialtracker.data.repositories.ExpenseRepository

class ExpenseViewModel : ViewModel() {

    private val expenseRepository = ExpenseRepository()

    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List<Expense>> = _expenses

    init {
        listenForExpenses()
    }

    private fun listenForExpenses() {
        expenseRepository.listenForExpenses(
            onSuccess = { expenses ->
                _expenses.value = expenses
            },
            onFailure = { 
                // Handle error
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        expenseRepository.unregisterListener()
    }
}