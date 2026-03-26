package com.example.financialtracker.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.financialtracker.R
import com.example.financialtracker.data.model.Friend
import java.util.Locale

class FriendsAdapter(
    private val context: Context,
    private val friends: List<Friend>,
    private val onRemoveClick: (String) -> Unit,
    private val onFriendClick: (String) -> Unit
) : ArrayAdapter<Friend>(context, 0, friends) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val friend = friends[position]
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.friend_list_item, parent, false)

        val nameTextView = view.findViewById<TextView>(R.id.friendNameTextView)
        val statusTextView = view.findViewById<TextView>(R.id.friendStatusTextView)
        val removeButton = view.findViewById<Button>(R.id.removeFriendButton)
        val avatarTextView = view.findViewById<TextView>(R.id.friendAvatar)
        val avatarImageView = view.findViewById<ImageView>(R.id.friendAvatarImage)
        val statusDot = view.findViewById<View>(R.id.statusDot)

        nameTextView.text = friend.username
        val status = friend.status
        statusTextView.text = status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        
        // Load profile picture if exists
        if (friend.profileImageUrl.isNotEmpty()) {
            avatarImageView.visibility = View.VISIBLE
            avatarTextView.visibility = View.GONE
            Glide.with(context)
                .load(friend.profileImageUrl)
                .circleCrop()
                .into(avatarImageView)
        } else {
            avatarImageView.visibility = View.GONE
            avatarTextView.visibility = View.VISIBLE
            // Set initials
            avatarTextView.text = getInitials(friend.username)
        }

        // Update status dot and text color
        if (status.equals("online", ignoreCase = true)) {
            statusDot.setBackgroundResource(R.drawable.status_dot_online)
            statusTextView.setTextColor(context.getColor(R.color.online_status))
        } else {
            statusDot.setBackgroundResource(R.drawable.status_dot_offline)
            statusTextView.setTextColor(context.getColor(R.color.textSecondary))
        }

        removeButton.setOnClickListener {
            onRemoveClick(friend.username)
        }

        view.setOnClickListener {
            onFriendClick(friend.username)
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