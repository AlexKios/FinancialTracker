package com.example.financialtracker.data.repositories

import com.example.financialtracker.data.model.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ExpenseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var expenseListener: ListenerRegistration? = null

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

    fun listenForExpenses(onSuccess: (List<Expense>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            expenseListener = db.collection("users").document(userId).collection("expenses")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        onFailure(e)
                        return@addSnapshotListener
                    }
                    val expenses = snapshot?.toObjects(Expense::class.java) ?: emptyList()
                    onSuccess(expenses)
                }
        } else {
            onFailure(Exception("User not logged in"))
        }
    }

    fun unregisterListener() {
        expenseListener?.remove()
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
                    val expense = document.toObject(Expense::class.java)
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