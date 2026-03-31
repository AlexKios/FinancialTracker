package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financialtracker.data.model.User
import com.example.financialtracker.data.model.Income
import com.example.financialtracker.data.model.Expense
import com.example.financialtracker.data.model.UserSettings
import com.example.financialtracker.data.repositories.ExpenseRepository
import com.example.financialtracker.data.repositories.IncomeRepository
import com.example.financialtracker.data.repositories.SettingsRepository
import com.example.financialtracker.data.repositories.UserRepository
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class FriendProgressData(
    val id: String,
    val name: String,
    val profileImageUrl: String,
    val progress: Int
)

class MainViewModel : ViewModel() {

    private val userRepo = UserRepository()
    private val incomeRepo = IncomeRepository()
    private val expenseRepo = ExpenseRepository()
    private val settingsRepo = SettingsRepository()

    private var currentUser: User? = null
    private var currentIncomes: List<Income> = emptyList()
    private var currentExpenses: List<Expense> = emptyList()

    private val _budget = MutableLiveData<Double>()
    val budget: LiveData<Double> = _budget

    private val _allTransactions = MutableLiveData<List<Triple<String, Double, String>>>()
    val allTransactions: LiveData<List<Triple<String, Double, String>>> get() = _allTransactions

    private val _chartData = MutableLiveData<List<Entry>>()
    val chartData: LiveData<List<Entry>> = _chartData

    private val _friendsData = MutableLiveData<List<FriendProgressData>>()
    val friendsData: LiveData<List<FriendProgressData>> = _friendsData

    private val _userSettings = MutableLiveData<UserSettings>()
    val userSettings: LiveData<UserSettings> = _userSettings

    private var selectedChartMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedChartYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    private val friendsMap = ConcurrentHashMap<String, FriendProgressData>()

    fun loadUserData() {
        viewModelScope.launch {
            try {
                currentUser = userRepo.getCurrentUser()
                loadSettings()
                listenForTransactions()
                loadFriendsData()
            } catch (e: Exception) {
            }
        }
    }

    private suspend fun loadSettings() {
        try {
            _userSettings.value = settingsRepo.getUserSettings()
        } catch (e: Exception) {
        }
    }

    private fun listenForTransactions() {
        viewModelScope.launch {
            combine(
                incomeRepo.listenForIncomes(),
                expenseRepo.listenForExpenses()
            ) { incomes, expenses ->
                currentIncomes = incomes
                currentExpenses = expenses
                updateUI()
            }.catch {
            }.collect {}
        }
    }

    private fun updateUI() {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val combinedList = mutableListOf<Triple<String, Double, Date>>()

        currentIncomes.forEach { income ->
            val date = income.date?.toDate() ?: Date(0)
            combinedList.add(Triple("Income: ${income.source}", income.amount, date))
        }

        currentExpenses.forEach { expense ->
            val date = expense.date?.toDate() ?: Date(0)
            combinedList.add(Triple("Expense: ${expense.category}", expense.amount, date))
        }

        combinedList.sortByDescending { it.third }

        val transactions = combinedList.map { item ->
            Triple(item.first, item.second, formatter.format(item.third))
        }

        _allTransactions.value = transactions

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val monthlyExpenses = currentExpenses.filter { expense ->
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

        refreshChartData()
    }

    fun loadFriendsData() {
        friendsMap.clear()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        currentUser?.friends?.forEach { friendId ->
            viewModelScope.launch {
                try {
                    val friend = userRepo.getUserById(friendId)
                    expenseRepo.listenForExpensesForUser(friend.uid).collect { expenses ->
                        val monthlyExpenses = expenses.filter { expense ->
                            val expenseCalendar = Calendar.getInstance()
                            expense.date?.let { timestamp ->
                                expenseCalendar.time = timestamp.toDate()
                                expenseCalendar.get(Calendar.MONTH) == currentMonth &&
                                expenseCalendar.get(Calendar.YEAR) == currentYear
                            } ?: false
                        }
                        val totalMonthlyExpenses = monthlyExpenses.sumOf { it.amount }

                        val budgetPercentage = if (friend.budget > 0) {
                            ((friend.budget - totalMonthlyExpenses) / friend.budget * 100).toInt().coerceIn(0, 100)
                        } else {
                            0
                        }

                        friendsMap[friend.uid] = FriendProgressData(
                            friend.uid, 
                            friend.name, 
                            friend.profileImageUrl, 
                            budgetPercentage
                        )
                        _friendsData.postValue(friendsMap.values.toList())
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    fun updateChartData(month: Int, year: Int) {
        selectedChartMonth = month
        selectedChartYear = year
        refreshChartData()
    }

    private fun refreshChartData() {
        val dailySpending = mutableMapOf<Int, Double>()
        val calendar = Calendar.getInstance()
        calendar.set(selectedChartYear, selectedChartMonth, 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 1..daysInMonth) {
            dailySpending[i] = 0.0
        }

        currentExpenses.forEach { expense ->
            val expenseCalendar = Calendar.getInstance()
            expense.date?.let { timestamp ->
                expenseCalendar.time = timestamp.toDate()
                if (expenseCalendar.get(Calendar.MONTH) == selectedChartMonth && 
                    expenseCalendar.get(Calendar.YEAR) == selectedChartYear) {
                    val dayOfMonth = expenseCalendar.get(Calendar.DAY_OF_MONTH)
                    dailySpending[dayOfMonth] = (dailySpending[dayOfMonth] ?: 0.0) + expense.amount
                }
            }
        }

        val entries = dailySpending.toSortedMap().map { (day, total) ->
            Entry(day.toFloat(), total.toFloat())
        }

        _chartData.value = entries
    }

    fun calculateBudgetPercentage(remaining: Double): Int {
        val totalBudget = currentUser?.budget ?: 1.0
        if (totalBudget <= 0.0) return 0
        return ((remaining / totalBudget) * 100).toInt().coerceIn(0, 100)
    }

    fun setBudget(budget: Double) {
        viewModelScope.launch {
            try {
                userRepo.updateBudget(budget)
                currentUser?.budget = budget
                updateUI()
            } catch (e: Exception) {
            }
        }
    }
}
