package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financialtracker.data.model.Income
import com.example.financialtracker.data.repositories.IncomeRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class IncomeViewModel : ViewModel() {

    private val incomeRepository = IncomeRepository()

    private val _incomes = MutableLiveData<List<Income>>()
    val incomes: LiveData<List<Income>> = _incomes

    init {
        listenForIncomes()
    }

    private fun listenForIncomes() {
        viewModelScope.launch {
            incomeRepository.listenForIncomes()
                .catch { /* Handle error */ }
                .collect { incomes ->
                    _incomes.value = incomes
                }
        }
    }

    fun deleteIncome(incomeId: String) {
        viewModelScope.launch {
            try {
                incomeRepository.deleteIncome(incomeId)
            } catch (e: Exception) {
            }
        }
    }
}
