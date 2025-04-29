package com.example.financialtracker.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.ArrayAdapter
import com.example.financialtracker.R

// import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.firestore.FirebaseFirestore

class FriendsActivity : BaseActivity() {

    override val navMenuItemId = R.id.nav_friends
    private lateinit var friendsListView: ListView
    private val friendsList = mutableListOf<String>()  // This will be populated with friend names (or IDs)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_friends, findViewById(R.id.content_container), true)


        friendsListView = findViewById(R.id.friendsListView)

        // You can get friends from Firebase or any other source.
        loadFriends()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, friendsList)
        friendsListView.adapter = adapter

        friendsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedFriend = friendsList[position]
            startChat(selectedFriend)
        }
    }

    private fun loadFriends() {
        // For now, let's mock some friends
        friendsList.add("Friend 1")
        friendsList.add("Friend 2")
        friendsList.add("Friend 3")

        // In the future, you can retrieve the list from Firebase
        // val db = FirebaseFirestore.getInstance()
        // val userId = FirebaseAuth.getInstance().uid ?: return
        // db.collection("users").document(userId)
        //    .collection("friends")
        //    .get()
        //    .addOnSuccessListener { documents ->
        //        for (document in documents) {
        //            friendsList.add(document.getString("name") ?: "Unknown")
        //        }
        //    }
    }

    private fun startChat(friendName: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("friend_name", friendName)
        startActivity(intent)
    }
}