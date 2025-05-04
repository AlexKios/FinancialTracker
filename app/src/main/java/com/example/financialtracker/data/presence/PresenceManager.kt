package com.example.financialtracker.data.presence

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class PresenceManager(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : DefaultLifecycleObserver {

    private val userRef: DocumentReference
        get() {
            val uid = auth.currentUser?.uid
            return if (uid != null) {
                db.collection("users").document(uid)
            } else {
                throw IllegalStateException("User is not authenticated")
            }
        }

    private var listenerRegistration: ListenerRegistration? = null

    override fun onStart(owner: LifecycleOwner) {

        userRef.update("online", true)

        listenerRegistration = userRef.addSnapshotListener { _, error ->
            if (error != null) {
                return@addSnapshotListener
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        userRef.update("online", false)
        listenerRegistration?.remove()
    }
}