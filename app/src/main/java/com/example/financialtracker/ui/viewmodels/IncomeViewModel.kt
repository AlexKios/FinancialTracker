package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financialtracker.data.model.Income
import com.example.financialtracker.data.repositories.IncomeRepository

class IncomeViewModel : ViewModel() {

    private val incomeRepository = IncomeRepository()

    private val _incomes = MutableLiveData<List<Income>>()
    val incomes: LiveData<List<Income>> = _incomes

    init {
        listenForIncomes()
    }

    private fun listenForIncomes() {
        incomeRepository.listenForIncomes(
            onSuccess = { incomes ->
                _incomes.value = incomes
            },
            onFailure = { 
                // Handle error
            }
        )
    }

    fun deleteIncome(incomeId: String) {
        incomeRepository.deleteIncome(incomeId,
            onSuccess = {
                // Income deleted successfully, the listener will update the list
            },
            onFailure = {
                // Handle error
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        incomeRepository.unregisterListener()
    }
}