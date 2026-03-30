package com.example.financialtracker.data.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.financialtracker.R
import com.example.financialtracker.ui.activities.ChatActivity
import com.example.financialtracker.ui.viewmodels.FriendProgressData

class HomeFriendsAdapter : ListAdapter<FriendProgressData, HomeFriendsAdapter.FriendViewHolder>(FriendDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_progress, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val friendProgressBar: ProgressBar = itemView.findViewById(R.id.friendProgressBar)
        private val friendNameTextView: TextView = itemView.findViewById(R.id.friendNameTextView)
        private val friendProfileImageView: ImageView = itemView.findViewById(R.id.friendProfileImageView)

        fun bind(friend: FriendProgressData) {
            friendProgressBar.progress = friend.progress
            friendNameTextView.text = friend.name

            if (friend.profileImageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(friend.profileImageUrl)
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .into(friendProfileImageView)
            } else {
                friendProfileImageView.setImageResource(R.drawable.user)
            }

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ChatActivity::class.java).apply {
                    putExtra("friend_uid", friend.id)
                    putExtra("friend_name", friend.name)
                    putExtra("friend_profile_image_url", friend.profileImageUrl)
                }
                itemView.context.startActivity(intent)
            }
        }
    }

    class FriendDiffCallback : DiffUtil.ItemCallback<FriendProgressData>() {
        override fun areItemsTheSame(oldItem: FriendProgressData, newItem: FriendProgressData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FriendProgressData, newItem: FriendProgressData): Boolean {
            return oldItem == newItem
        }
    }
}