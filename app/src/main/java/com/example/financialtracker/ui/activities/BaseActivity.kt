package com.example.financialtracker.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financialtracker.R
import com.example.financialtracker.data.helper.ChatSessionTracker
import com.example.financialtracker.data.helper.NotificationHelper
import com.example.financialtracker.data.model.SearchResult
import com.example.financialtracker.data.model.SearchResultType
import com.example.financialtracker.data.repositories.ChatRepository
import com.example.financialtracker.data.repositories.ExpenseRepository
import com.example.financialtracker.data.repositories.IncomeRepository
import com.example.financialtracker.data.repositories.UserRepository
import com.example.financialtracker.data.adapter.SearchAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseActivity : AppCompatActivity() {

    protected open val navMenuItemId: Int = 0
    private val chatRepository = ChatRepository()
    private lateinit var notificationHelper: NotificationHelper
    private var notificationSentForMessage = mutableSetOf<String>()
    private lateinit var searchView: SearchView
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchAdapter: SearchAdapter
    private val userRepository = UserRepository()
    private val incomeRepository = IncomeRepository()
    private val expenseRepository = ExpenseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeSync()
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

        searchView = findViewById(R.id.search_view)
        setupSearchView()
        setupSearchRecyclerView()

        notificationHelper = NotificationHelper(this)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            attachGlobalChatListener(currentUserId)
        }
    }

    private fun applyThemeSync() {
        val prefs = getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("darkMode", false)
        val mode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode)
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

    private fun setupSearchRecyclerView() {
        searchResultsRecyclerView = findViewById(R.id.search_results_recycler_view)
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchAdapter = SearchAdapter(emptyList()) { searchResult ->
            when (searchResult.type) {
                SearchResultType.FRIEND -> {
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("friend_uid", searchResult.id)
                    intent.putExtra("friend_name", searchResult.title)
                    intent.putExtra("friend_profile_image_url", searchResult.imageUrl)
                    startActivity(intent)
                }
                SearchResultType.INCOME -> {
                    val intent = Intent(this, EditIncomeActivity::class.java)
                    intent.putExtra("incomeId", searchResult.id)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                SearchResultType.EXPENSE -> {
                    val intent = Intent(this, EditExpenseActivity::class.java)
                    intent.putExtra("expenseId", searchResult.id)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }
        searchResultsRecyclerView.adapter = searchAdapter
    }

    private fun setupSearchView() {
        searchView.setOnClickListener {
            searchView.isIconified = false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    searchResultsRecyclerView.visibility = View.GONE
                } else {
                    searchResultsRecyclerView.visibility = View.VISIBLE
                    performSearch(newText)
                }
                return true
            }
        })
    }

    private fun performSearch(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val searchResults = mutableListOf<SearchResult>()

            val friends = userRepository.getFriends()
            friends.forEach { friend ->
                if (friend.username.contains(query, ignoreCase = true)) {
                    searchResults.add(
                        SearchResult(
                            id = friend.userId,
                            title = friend.username,
                            subtitle = "Friend",
                            type = SearchResultType.FRIEND,
                            imageUrl = friend.profileImageUrl
                        )
                    )
                }
            }

            val incomes = incomeRepository.getIncomes(userId)
            incomes.forEach { income ->
                if (income.source.contains(query, ignoreCase = true)) {
                    searchResults.add(
                        SearchResult(
                            id = income.id,
                            title = income.source,
                            subtitle = "Income",
                            type = SearchResultType.INCOME
                        )
                    )
                }
            }

            val expenses = expenseRepository.getExpenses(userId)
            expenses.forEach { expense ->
                if (expense.category.contains(query, ignoreCase = true)) {
                    searchResults.add(
                        SearchResult(
                            id = expense.id,
                            title = expense.category,
                            subtitle = "Expense",
                            type = SearchResultType.EXPENSE
                        )
                    )
                }
            }

            withContext(Dispatchers.Main) {
                searchAdapter.updateData(searchResults)
            }
        }
    }

    private fun attachGlobalChatListener(currentUserId: String) {
        chatRepository.listenToAllIncomingMessages(currentUserId) { message, senderId, chatId ->
            if (!notificationSentForMessage.contains(message.timestamp.toString())) {
                if (!isInActiveChat(currentUserId, senderId)) {
                    notificationHelper.sendNotification(message.senderId, message.messageContent, chatId)
                    notificationSentForMessage.add(message.timestamp.toString())
                    chatRepository.updateMessageStatus(chatId, message, "received")
                }            }
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