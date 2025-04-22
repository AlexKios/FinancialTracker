package com.example.financialtracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.financialtracker.R
// import com.google.firebase.firestore.FirebaseFirestore
// import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.firestore.FieldValue

class ScanFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    private fun onQRCodeScanned(friendId: String) {
        // val db = FirebaseFirestore.getInstance()
        // val myId = FirebaseAuth.getInstance().uid ?: return
        // db.collection("users").document(myId)
        //     .collection("friends").document(friendId)
        //     .set(mapOf("addedAt" to FieldValue.serverTimestamp()))
    }
}