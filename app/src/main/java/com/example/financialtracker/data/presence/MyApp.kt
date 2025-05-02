package com.example.financialtracker.data.presence

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val presenceManager = PresenceManager(
            FirebaseFirestore.getInstance(),
            FirebaseAuth.getInstance()
        )

        ProcessLifecycleOwner.get().lifecycle.addObserver(presenceManager)
    }
}