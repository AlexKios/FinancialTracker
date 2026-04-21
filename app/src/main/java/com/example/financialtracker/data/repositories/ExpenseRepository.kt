package com.example.financialtracker.data.repositories

import com.example.financialtracker.data.model.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ExpenseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getExpenses(userId: String): List<Expense> {
        val snapshot = db.collection("users").document(userId).collection("expenses")
            .get()
            .await()
        return snapshot.documents.mapNotNull {
            it.toObject(Expense::class.java)?.copy(id = it.id)
        }
    }

    suspend fun addExpense(expense: Expense) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        db.collection("users").document(userId).collection("expenses")
            .add(expense)
            .await()
    }

    suspend fun updateExpense(expense: Expense) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        db.collection("users").document(userId).collection("expenses").document(expense.id)
            .set(expense)
            .await()
    }

    suspend fun deleteExpense(expenseId: String) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        db.collection("users").document(userId).collection("expenses").document(expenseId)
            .delete()
            .await()
    }

    suspend fun getRecurringExpenses(): List<Expense> {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val snapshot = db.collection("users").document(userId).collection("expenses")
            .whereEqualTo("isRecurring", true)
            .get()
            .await()
        return snapshot.documents.mapNotNull {
            it.toObject(Expense::class.java)?.copy(id = it.id)
        }
    }

    fun listenForExpenses(): Flow<List<Expense>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            close(Exception("User not logged in"))
            return@callbackFlow
        }

        val registration = db.collection("users").document(userId).collection("expenses")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.documents?.mapNotNull {
                    it.toObject(Expense::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(expenses)
            }

        awaitClose { registration.remove() }
    }

    fun listenForExpensesForUser(userId: String): Flow<List<Expense>> = callbackFlow {
        val registration = db.collection("users").document(userId).collection("expenses")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.documents?.mapNotNull {
                    it.toObject(Expense::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { registration.remove() }
    }

    suspend fun getExpenseById(id: String): Expense {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val document = db.collection("users").document(userId).collection("expenses").document(id)
            .get()
            .await()
        return document.toObject(Expense::class.java)?.copy(id = document.id)
            ?: throw Exception("Expense not found")
    }
}
