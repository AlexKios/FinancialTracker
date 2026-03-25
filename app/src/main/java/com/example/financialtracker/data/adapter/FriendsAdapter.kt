package com.example.financialtracker.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.example.financialtracker.R
import java.util.Locale

class FriendsAdapter(
    private val context: Context,
    private val friends: List<Pair<String, String>>,
    private val onRemoveClick: (String) -> Unit,
    private val onFriendClick: (String) -> Unit
) : ArrayAdapter<Pair<String, String>>(context, 0, friends) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val (username, status) = friends[position]
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.friend_list_item, parent, false)

        val nameTextView = view.findViewById<TextView>(R.id.friendNameTextView)
        val statusTextView = view.findViewById<TextView>(R.id.friendStatusTextView)
        val removeButton = view.findViewById<Button>(R.id.removeFriendButton)
        val avatarTextView = view.findViewById<TextView>(R.id.friendAvatar)
        val statusDot = view.findViewById<View>(R.id.statusDot)

        nameTextView.text = username
        statusTextView.text = status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        
        // Set initials
        avatarTextView.text = getInitials(username)

        // Update status dot and text color
        if (status.equals("online", ignoreCase = true)) {
            statusDot.setBackgroundResource(R.drawable.status_dot_online)
            statusTextView.setTextColor(context.getColor(R.color.online_status))
        } else {
            statusDot.setBackgroundResource(R.drawable.status_dot_offline)
            statusTextView.setTextColor(context.getColor(R.color.textSecondary))
        }

        removeButton.setOnClickListener {
            onRemoveClick(username)
        }

        view.setOnClickListener {
            onFriendClick(username)
        }

        return view
    }

    private fun getInitials(name: String): String {
        val parts = name.split(" ")
        return if (parts.size >= 2) {
            (parts[0].take(1) + parts[1].take(1)).uppercase()
        } else {
            name.take(2).uppercase()
        }
    }
}