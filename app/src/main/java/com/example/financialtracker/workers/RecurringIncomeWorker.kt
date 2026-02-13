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
                    val recurringDateCalendar = Calendar.getInstance().apply {
                        time = recurringDate.toDate()
                    }

                    if (now.after(recurringDateCalendar)) {
                        // Add a new income record
                        val newIncome = income.copy(
                            id = "", // Firestore will generate a new ID
                            date = Timestamp.now(),
                            isRecurring = false, // The new record is not a recurring template
                            recurringDate = null
                        )
                        incomeRepository.addIncome(newIncome)

                        // Update the recurring income's next date
                        val nextRecurringDate = Calendar.getInstance().apply {
                            time = recurringDate.toDate()
                            add(Calendar.MONTH, 1)
                        }
                        incomeRepository.updateIncome(income.id, mapOf("recurringDate" to Timestamp(nextRecurringDate.time)))
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
