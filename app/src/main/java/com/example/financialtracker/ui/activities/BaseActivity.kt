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
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
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
import com.example.financialtracker.data.presence.PresenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

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
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            if (menuItem.itemId == navMenuItemId) return@setOnItemSelectedListener true

            when (menuItem.itemId) {
                R.id.nav_income -> {
                    startActivity(Intent(this, IncomeActivity::class.java))
                    true
                }
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_friends -> {
                    startActivity(Intent(this, FriendsActivity::class.java))
                    true
                }
                R.id.nav_expense -> {
                    startActivity(Intent(this, ExpenseActivity::class.java))
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

        setupPresenceManager()
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

    private fun setupPresenceManager() {
        val presenceManager = PresenceManager(
            FirebaseFirestore.getInstance(),
            FirebaseAuth.getInstance()
        )
        ProcessLifecycleOwner.get().lifecycle.addObserver(presenceManager)
    }

    private fun setupSearchRecyclerView() {
        searchResultsRecyclerView = findViewById(R.id.search_results_recycler_view)
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchAdapter = SearchAdapter(emptyList()) { searchResult ->
            val intent = when (searchResult.type) {
                SearchResultType.FRIEND -> {
                    Intent(this, ChatActivity::class.java).apply {
                        putExtra("friend_uid", searchResult.id)
                        putExtra("friend_name", searchResult.title)
                        putExtra("friend_profile_image_url", searchResult.imageUrl)
                    }
                }
                SearchResultType.INCOME -> {
                    Intent(this, EditIncomeActivity::class.java).apply {
                        putExtra("incomeId", searchResult.id)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
                SearchResultType.EXPENSE -> {
                    Intent(this, EditExpenseActivity::class.java).apply {
                        putExtra("expenseId", searchResult.id)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
            }
            startActivity(intent)
        }
        searchResultsRecyclerView.adapter = searchAdapter
    }

    private fun setupSearchView() {
        searchView.setOnClickListener {
            searchView.isIconified = false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

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
        lifecycleScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val searchResults = mutableListOf<SearchResult>()

            try {
                val friends = userRepository.getFriends()
                friends.filter { it.username.contains(query, ignoreCase = true) }
                    .forEach { friend ->
                        searchResults.add(SearchResult(friend.userId, friend.username, "Friend", SearchResultType.FRIEND, friend.profileImageUrl))
                    }

                val incomes = incomeRepository.getIncomes(userId)
                incomes.filter { it.source.contains(query, ignoreCase = true) }
                    .forEach { income ->
                        searchResults.add(SearchResult(income.id, income.source, "Income", SearchResultType.INCOME))
                    }

                val expenses = expenseRepository.getExpenses(userId)
                expenses.filter { it.category.contains(query, ignoreCase = true) }
                    .forEach { expense ->
                        searchResults.add(SearchResult(expense.id, expense.category, "Expense", SearchResultType.EXPENSE))
                    }

                searchAdapter.updateData(searchResults)
            } catch (e: Exception) {
            }
        }
    }

    private fun attachGlobalChatListener(currentUserId: String) {
        lifecycleScope.launch {
            chatRepository.listenToAllIncomingMessages(currentUserId)
                .catch {}
                .collect { (message, senderId, chatId) ->
                    if (!notificationSentForMessage.contains(message.timestamp.toString())) {
                        if (senderId != ChatSessionTracker.activeChatUserId) {
                            notificationHelper.sendNotification(message.senderId, message.messageContent, chatId)
                            notificationSentForMessage.add(message.timestamp.toString())
                            chatRepository.updateMessageStatus(chatId, message, "received")
                        }
                    }
                }
        }
    }
}
