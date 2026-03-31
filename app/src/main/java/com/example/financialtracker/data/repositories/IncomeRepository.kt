package com.example.financialtracker.data.repositories

import com.example.financialtracker.data.model.Income
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class IncomeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getIncomes(userId: String): List<Income> {
        val snapshot = db.collection("users").document(userId).collection("incomes")
            .get()
            .await()
        return snapshot.documents.mapNotNull {
            it.toObject(Income::class.java)?.copy(id = it.id)
        }
    }

    suspend fun addIncome(income: Income) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        db.collection("users").document(userId).collection("incomes")
            .add(income)
            .await()
    }

    suspend fun updateIncome(income: Income) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        db.collection("users").document(userId).collection("incomes").document(income.id)
            .set(income)
            .await()
    }

    suspend fun deleteIncome(incomeId: String) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        db.collection("users").document(userId).collection("incomes").document(incomeId)
            .delete()
            .await()
    }

    suspend fun getRecurringIncomes(): List<Income> {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val snapshot = db.collection("users").document(userId).collection("incomes")
            .whereEqualTo("isRecurring", true)
            .get()
            .await()
        return snapshot.documents.mapNotNull {
            it.toObject(Income::class.java)?.copy(id = it.id)
        }
    }

    fun listenForIncomes(): Flow<List<Income>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            close(Exception("User not logged in"))
            return@callbackFlow
        }

        val registration = db.collection("users").document(userId).collection("incomes")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val incomes = snapshot?.documents?.mapNotNull {
                    it.toObject(Income::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(incomes)
            }

        awaitClose { registration.remove() }
    }

    suspend fun getIncomeById(id: String): Income {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val document = db.collection("users").document(userId).collection("incomes").document(id)
            .get()
            .await()
        return document.toObject(Income::class.java)?.copy(id = document.id)
            ?: throw Exception("Income not found")
    }
}
