package com.example.financialtracker.data.repositories

import com.example.financialtracker.data.model.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class ExpenseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var expenseListener: ListenerRegistration? = null
    private val userExpenseListeners = mutableMapOf<String, ListenerRegistration>()

    suspend fun getExpenses(userId: String): List<Expense> {
        val snapshot = db.collection("users").document(userId).collection("expenses")
            .get()
            .await()
        return snapshot.documents.mapNotNull {
            it.toObject(Expense::class.java)?.copy(id = it.id)
        }
    }

    fun addExpense(expense: Expense, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("expenses")
                .add(expense)
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

    fun updateExpense(expense: Expense, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("expenses").document(expense.id)
                .set(expense)
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

    fun deleteExpense(expenseId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("expenses").document(expenseId)
                .delete()
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

    fun listenForExpenses(onSuccess: (List<Expense>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            expenseListener = db.collection("users").document(userId).collection("expenses")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        onFailure(e)
                        return@addSnapshotListener
                    }
                    val expenses = snapshot?.documents?.mapNotNull {
                        it.toObject(Expense::class.java)?.copy(id = it.id)
                    } ?: emptyList()
                    onSuccess(expenses)
                }
        } else {
            onFailure(Exception("User not logged in"))
        }
    }

    fun listenForExpensesForUser(userId: String, onSuccess: (List<Expense>) -> Unit, onFailure: (Exception) -> Unit) {
        val listener = db.collection("users").document(userId).collection("expenses")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onFailure(e)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.documents?.mapNotNull {
                    it.toObject(Expense::class.java)?.copy(id = it.id)
                } ?: emptyList()
                onSuccess(expenses)
            }
        userExpenseListeners[userId] = listener
    }

    fun unregisterCurrentUserListener() {
        expenseListener?.remove()
        expenseListener = null
    }

    fun unregisterUserListeners() {
        userExpenseListeners.forEach { (_, listener) ->
            listener.remove()
        }
        userExpenseListeners.clear()
    }

    fun unregisterListener() {
        unregisterCurrentUserListener()
        unregisterUserListeners()
    }

    fun getExpenseById(
        id: String,
        onSuccess: (Expense) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("expenses").document(id)
                .get()
                .addOnSuccessListener { document ->
                    val expense = document.toObject(Expense::class.java)?.copy(id = document.id)
                    if (expense != null) {
                        onSuccess(expense)
                    } else {
                        onFailure(Exception("Expense not found"))
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