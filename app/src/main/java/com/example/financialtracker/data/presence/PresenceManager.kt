package com.example.financialtracker.data.presence

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PresenceManager(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("online", true)
    }

    override fun onStop(owner: LifecycleOwner) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("online", false)
    }
}