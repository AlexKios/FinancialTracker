package com.example.financialtracker.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.financialtracker.data.repositories.ExpenseRepository
import com.google.firebase.Timestamp
import java.util.Calendar

class RecurringExpenseWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val expenseRepository = ExpenseRepository()

    override suspend fun doWork(): Result {
        return try {
            val recurringExpenses = expenseRepository.getRecurringExpenses()
            val now = Calendar.getInstance()

            for (expense in recurringExpenses) {
                expense.recurringDate?.let { recurringDate ->
                    val recurringDateCalendar = Calendar.getInstance().apply {
                        time = recurringDate.toDate()
                    }

                    while (now.after(recurringDateCalendar)) {
                        val newExpense = expense.copy(
                            id = "",
                            date = Timestamp(recurringDateCalendar.time),
                            isRecurring = false,
                            recurringDate = null
                        )
                        expenseRepository.addExpense(newExpense)

                        recurringDateCalendar.add(Calendar.MONTH, 1)

                        val updatedExpense = expense.copy(
                            recurringDate = Timestamp(recurringDateCalendar.time)
                        )
                        expenseRepository.updateExpense(updatedExpense)
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
