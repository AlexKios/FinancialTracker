package com.example.financialtracker.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.financialtracker.R
import com.example.financialtracker.data.helper.ChatSessionTracker
import com.example.financialtracker.data.helper.NotificationHelper
import com.example.financialtracker.data.repositories.ChatRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

abstract class BaseActivity : AppCompatActivity() {

    protected open val navMenuItemId: Int = 0
    private val chatRepository = ChatRepository()
    private lateinit var notificationHelper: NotificationHelper
    private var notificationSentForMessage = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_layout)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar).also { toolbar ->
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, AccountActivity::class.java))
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView2)
        bottomNavigationView.selectedItemId = navMenuItemId
        bottomNavigationView.setOnItemSelectedListener  { menuItem ->
            if (menuItem.itemId == navMenuItemId) return@setOnItemSelectedListener true

            when (menuItem.itemId) {
                R.id.nav_income -> {
                    val intent = Intent(this, IncomeActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_friends -> {
                    val intent = Intent(this, FriendsActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_expense -> {
                    val intent = Intent(this, ExpenseActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        notificationHelper = NotificationHelper(this)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            attachGlobalChatListener(currentUserId)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun attachGlobalChatListener(currentUserId: String) {
        chatRepository.listenToAllIncomingMessages(currentUserId) { message, senderId, chatId ->
            if (message.timestamp != null && !notificationSentForMessage.contains(message.timestamp.toString())) {
                if (!isInActiveChat(currentUserId, senderId)) {
                    notificationHelper.sendNotification(message.senderId, message.messageContent, chatId)
                    notificationSentForMessage.add(message.timestamp.toString())
                    chatRepository.updateMessageStatus(chatId, message, "received")
                }
            }
        }
    }

    private fun isInActiveChat(currentUserId: String, senderId: String): Boolean {
        return senderId == ChatSessionTracker.activeChatUserId
    }

    override fun onDestroy() {
        super.onDestroy()
        chatRepository.removeGlobalListener()
    }
}