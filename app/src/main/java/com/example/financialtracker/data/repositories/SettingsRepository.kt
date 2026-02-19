package com.example.financialtracker.data.repositories

import com.example.financialtracker.data.model.UserSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun saveUserSettings(settings: UserSettings, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("settings").document("user_settings")
                .set(settings)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
        } else {
            onFailure(Exception("User not authenticated"))
        }
    }

    fun getUserSettings(
        onSuccess: (UserSettings) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("settings").document("user_settings")
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val settings = document.toObject(UserSettings::class.java)
                        onSuccess(settings ?: UserSettings())
                    } else {
                        onSuccess(UserSettings())
                    }
                }
                .addOnFailureListener { onFailure(it) }
        } else {
            onFailure(Exception("User not authenticated"))
        }
    }

}
