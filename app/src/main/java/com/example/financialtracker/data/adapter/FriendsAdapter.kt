package com.example.financialtracker.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.example.financialtracker.R

class FriendsAdapter(
    private val context: Context,
    private val friends: List<String>,
    private val onRemoveClick: (String) -> Unit
) : ArrayAdapter<String>(context, 0, friends) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val friend = friends[position]
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.friend_list_item, parent, false)

        val nameTextView = view.findViewById<TextView>(R.id.friendNameTextView)
        val removeButton = view.findViewById<Button>(R.id.removeFriendButton)

        nameTextView.text = friend

        removeButton.setOnClickListener {
            onRemoveClick(friend)
        }

        return view
    }
}