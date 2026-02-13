package com.example.financialtracker.data.repositories

import com.example.financialtracker.data.model.Income
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class IncomeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var incomeListener: ListenerRegistration? = null

    fun addIncome(income: Income, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("incomes")
                .add(income)
                .addOnSuccessListener { 
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        } else {
            onFailure(Exception("User not logged in"))
        }
    }

    suspend fun addIncome(income: Income) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("incomes").add(income).await()
        } else {
            throw Exception("User not logged in")
        }
    }

    suspend fun getRecurringIncomes(): List<Income> {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val snapshot = db.collection("users").document(userId).collection("incomes")
            .whereEqualTo("isRecurring", true)
            .get()
            .await()
        return snapshot.toObjects(Income::class.java)
    }

    suspend fun updateIncome(incomeId: String, updates: Map<String, Any>) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        db.collection("users").document(userId).collection("incomes").document(incomeId)
            .update(updates)
            .await()
    }

    fun listenForIncomes(onSuccess: (List<Income>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            incomeListener = db.collection("users").document(userId).collection("incomes")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        onFailure(e)
                        return@addSnapshotListener
                    }
                    val incomes = snapshot?.toObjects(Income::class.java) ?: emptyList()
                    onSuccess(incomes)
                }
        } else {
            onFailure(Exception("User not logged in"))
        }
    }

    fun unregisterListener() {
        incomeListener?.remove()
    }

    fun getIncomeById(
        id: String,
        onSuccess: (Income) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("incomes").document(id)
                .get()
                .addOnSuccessListener { document ->
                    val income = document.toObject(Income::class.java)
                    if (income != null) {
                        onSuccess(income)
                    } else {
                        onFailure(Exception("Income not found"))
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } else {
            onFailure(Exception("User not logged in"))
        }
    }
}