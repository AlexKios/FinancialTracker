package com.example.financialtracker.data.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.financialtracker.data.repositories.UserRepository
import com.example.financialtracker.ui.activities.ChatActivity

class NotificationHelper(private val context: Context) {

    private val channelId = "chat_channel"
    private val channelName = "Chat Notifications"
    private val userRepository = UserRepository()

    init {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(senderId: String, message: String, chatId: String) {

        userRepository.getUsername(senderId).addOnSuccessListener { document ->
            val senderName = document.getString("name") ?: "Unknown Sender"

            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra("chat_id", chatId)
                putExtra("friend_uid", senderId)
                putExtra("friend_name", senderName)
            }

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle("New message from $senderName")
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0, notification)
        }
            .addOnFailureListener {
                val notification = NotificationCompat.Builder(context, channelId)
                    .setContentTitle("New message from Unknown Sender")
                    .setContentText(message)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setAutoCancel(true)
                    .build()

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(0, notification)
            }
    }
}