package com.example.financialtracker.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.financialtracker.data.repositories.IncomeRepository
import com.google.firebase.Timestamp
import java.util.Calendar

class RecurringIncomeWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val incomeRepository = IncomeRepository()

    override suspend fun doWork(): Result {
        return try {
            val recurringIncomes = incomeRepository.getRecurringIncomes()
            val now = Calendar.getInstance()

            for (income in recurringIncomes) {
                income.recurringDate?.let { recurringDate ->
                    var recurringDateCalendar = Calendar.getInstance().apply {
                        time = recurringDate.toDate()
                    }

                    if (recurringDateCalendar.after(now)) {
                        val day = recurringDateCalendar.get(Calendar.DAY_OF_MONTH)
                        recurringDateCalendar.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), day)

                        if (recurringDateCalendar.after(now)) {
                            recurringDateCalendar.add(Calendar.MONTH, -1)
                        }
                    }

                    while (now.after(recurringDateCalendar)) {
                        val newIncome = income.copy(
                            id = "",
                            date = Timestamp(recurringDateCalendar.time),
                            isRecurring = false,
                            recurringDate = null
                        )
                        incomeRepository.addIncome(newIncome)

                        recurringDateCalendar.add(Calendar.MONTH, 1)

                        val updatedIncome = income.copy(
                            recurringDate = Timestamp(recurringDateCalendar.time)
                        )
                        incomeRepository.updateIncome(updatedIncome)
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
