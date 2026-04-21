package com.example.financialtracker

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.financialtracker.data.helper.CloudinaryClient
import com.example.financialtracker.data.helper.PresenceManager
import com.example.financialtracker.workers.RecurringExpenseWorker
import com.example.financialtracker.workers.RecurringIncomeWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class FinancialTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        initializeCloudinary()
        setupRecurringWorkers()
        setupPresenceManager()
    }

    private fun initializeCloudinary() {
        CloudinaryClient.init(this)
    }

    private fun setupPresenceManager() {
        val presenceManager = PresenceManager(
            FirebaseFirestore.getInstance(),
            FirebaseAuth.getInstance()
        )
        ProcessLifecycleOwner.get().lifecycle.addObserver(presenceManager)
    }

    private fun setupRecurringWorkers() {
        val recurringIncomeWorkRequest = 
            PeriodicWorkRequestBuilder<RecurringIncomeWorker>(1, TimeUnit.DAYS)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "recurring_income_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            recurringIncomeWorkRequest
        )

        val recurringExpenseWorkRequest = 
            PeriodicWorkRequestBuilder<RecurringExpenseWorker>(1, TimeUnit.DAYS)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "recurring_expense_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            recurringExpenseWorkRequest
        )
    }
}
