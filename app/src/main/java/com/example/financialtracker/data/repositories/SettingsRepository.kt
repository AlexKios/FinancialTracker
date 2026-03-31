package com.example.financialtracker.data.repositories

import com.example.financialtracker.data.model.UserSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SettingsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun saveUserSettings(settings: UserSettings) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        db.collection("users").document(userId)
            .collection("settings").document("user_settings")
            .set(settings)
            .await()
    }

    suspend fun getUserSettings(): UserSettings {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        val document = db.collection("users").document(userId)
            .collection("settings").document("user_settings")
            .get()
            .await()
        return if (document.exists()) {
            document.toObject(UserSettings::class.java) ?: UserSettings()
        } else {
            UserSettings()
        }
    }
}
