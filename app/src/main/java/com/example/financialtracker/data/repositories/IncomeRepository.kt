package com.example.financialtracker.data.repositories

import com.example.financialtracker.data.model.Income
import com.google.firebase.firestore.FirebaseFirestore

class IncomeRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getIncomeById(
        id: String,
        onSuccess: (Income) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("incomes").document(id)
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
    }
}