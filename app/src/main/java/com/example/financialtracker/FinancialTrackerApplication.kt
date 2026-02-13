package com.example.financialtracker

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.financialtracker.workers.RecurringIncomeWorker
import java.util.concurrent.TimeUnit

class FinancialTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupRecurringIncomeWorker()
    }

    private fun setupRecurringIncomeWorker() {
        val recurringIncomeWorkRequest = 
            PeriodicWorkRequestBuilder<RecurringIncomeWorker>(1, TimeUnit.DAYS)
                .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "recurring_income_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            recurringIncomeWorkRequest
        )
    }
}
