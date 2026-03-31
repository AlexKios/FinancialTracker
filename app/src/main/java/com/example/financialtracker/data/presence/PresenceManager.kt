package com.example.financialtracker.data.presence

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PresenceManager(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : DefaultLifecycleObserver {

    private var isAppInForeground = false

    init {
        auth.addAuthStateListener {
            syncPresence()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        isAppInForeground = true
        syncPresence()
    }

    override fun onStop(owner: LifecycleOwner) {
        isAppInForeground = false
        syncPresence()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        isAppInForeground = false
        syncPresence()
    }

    override fun onPause(owner: LifecycleOwner) {
        isAppInForeground = false
        syncPresence()
    }

    private fun syncPresence() {
        val uid = auth.currentUser?.uid ?: return
        
        val updates = mapOf(
            "online" to isAppInForeground
        )

        db.collection("users").document(uid)
            .update(updates)
            .addOnFailureListener { e ->
                Log.e("PresenceManager", "Failed to update presence for user $uid", e)
            }
    }
}
