package com.example.financialtracker.data.presence

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.financialtracker.data.helper.CloudinaryClient
import com.example.financialtracker.workers.RecurringIncomeWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        CloudinaryClient.init(this)

        val presenceManager = PresenceManager(
            FirebaseFirestore.getInstance(),
            FirebaseAuth.getInstance()
        )

        ProcessLifecycleOwner.get().lifecycle.addObserver(presenceManager)
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