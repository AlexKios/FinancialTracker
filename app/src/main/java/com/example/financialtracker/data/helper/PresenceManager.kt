package com.example.financialtracker.data.helper

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase

class PresenceManager(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : DefaultLifecycleObserver {

    private val realtimeDb = FirebaseDatabase.getInstance()

    init {
        auth.addAuthStateListener {
            syncPresence(isAppInForeground)
        }
    }

    private var isAppInForeground = false

    override fun onStart(owner: LifecycleOwner) {
        isAppInForeground = true
        syncPresence(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        isAppInForeground = false
        syncPresence(false)
    }

    private fun syncPresence(online: Boolean) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .update("online", online)
            .addOnFailureListener { e ->
                Log.e("PresenceManager", "Firestore update failed", e)
            }

        val statusRef = realtimeDb.getReference("status/$uid")
        if (online) {
            statusRef.setValue(true)
            statusRef.onDisconnect().setValue(false)
        } else {
            statusRef.setValue(false)
        }
    }
}
