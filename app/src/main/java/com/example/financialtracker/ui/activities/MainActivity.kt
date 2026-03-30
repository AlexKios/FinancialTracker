package com.example.financialtracker.ui.activities

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financialtracker.R
import com.example.financialtracker.data.adapter.HomeFriendsAdapter
import com.example.financialtracker.data.adapter.TransactionAdapter
import com.example.financialtracker.ui.viewmodels.MainViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Calendar
import java.util.Locale

class MainActivity : BaseActivity() {

    override val navMenuItemId = R.id.nav_home
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewAmount: TextView
    private lateinit var viewModel: MainViewModel
    private lateinit var setBudgetButton: Button
    private lateinit var lineChart: LineChart
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var friendsAdapter: HomeFriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contentContainer = findViewById<android.view.ViewGroup>(R.id.content_container)
        if (contentContainer != null) {
            layoutInflater.inflate(R.layout.activity_main, contentContainer, true)
        }

        progressBar = findViewById(R.id.progressBudget)
        textViewAmount = findViewById(R.id.remainingBudget)
        setBudgetButton = findViewById(R.id.setBudgetButton)
        lineChart = findViewById(R.id.lineChart)

        // Initialize RecyclerViews
        val rvTransactions = findViewById<RecyclerView>(R.id.rvTransactions)
        rvTransactions?.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(emptyList())
        rvTransactions?.adapter = transactionAdapter

        val rvFriends = findViewById<RecyclerView>(R.id.rvFriendsProgress)
        friendsAdapter = HomeFriendsAdapter()
        rvFriends?.adapter = friendsAdapter

        textViewAmount.text = ""

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        observeViewModel()
        viewModel.loadUserData()

        setBudgetButton.setOnClickListener {
            showSetBudgetDialog()
        }

        lineChart.setOnClickListener {
            showMonthPicker()
        }
    }

    private fun showMonthPicker() {
        val months = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, months)

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_month_picker, null)
        val listView = dialogView.findViewById<ListView>(R.id.listView)
        listView.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("Select a Month")
            .setView(dialogView)
            .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            viewModel.updateChartData(position, year)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showSetBudgetDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_set_budget, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextBudget)

        builder.setView(dialogLayout)
        builder.setPositiveButton("OK") { _, _ ->
            val budgetString = editText.text.toString()
            if (budgetString.isNotEmpty()) {
                val budget = budgetString.toDouble()
                viewModel.setBudget(budget)
            }
        }
        builder.setNegativeButton("Cancel") { _, _ ->
        }
        builder.show()
    }

    private fun observeViewModel() {
        viewModel.budget.observe(this) { remaining ->
            textViewAmount.text = getString(R.string.amount_format, remaining)
            val percentage = viewModel.calculateBudgetPercentage(remaining)
            updateProgressBar(remaining, percentage)
        }

        viewModel.allTransactions.observe(this) { transactions ->
            transactionAdapter.updateData(transactions)
        }

        viewModel.userSettings.observe(this) { settings ->
            viewModel.chartData.value?.let { entries ->
                setupChartWithData(entries, settings.graphSize.toFloat())
            }
        }

        viewModel.chartData.observe(this) { entries ->
            if (entries.isNotEmpty()) {
                val graphSize = viewModel.userSettings.value?.graphSize?.toFloat() ?: 16f
                setupChartWithData(entries, graphSize)
            }
        }

        viewModel.friendsData.observe(this) { friends ->
            friendsAdapter.submitList(friends)
        }
    }

    private fun setupChartWithData(entries: List<com.github.mikephil.charting.data.Entry>, graphSize: Float) {
        setupChart(lineChart, graphSize)
        val dataSet = LineDataSet(entries, "Daily Spending")
        dataSet.color = ContextCompat.getColor(this, R.color.primary)
        dataSet.valueTextColor = ContextCompat.getColor(this, R.color.textPrimary)
        dataSet.valueTextSize = graphSize
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.primary))
        dataSet.circleHoleColor = ContextCompat.getColor(this, R.color.primary)
        dataSet.setDrawFilled(true)
        dataSet.fillColor = ContextCompat.getColor(this, R.color.primary)
        dataSet.fillAlpha = 100

        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                if (value == 0f) {
                    return ""
                }
                return String.format(Locale.US, "%.0f", value)
            }
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate()
    }

    private fun setupChart(chart: LineChart, textSize: Float) {
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = ContextCompat.getColor(this, R.color.textPrimary)
        xAxis.textSize = textSize
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.textColor = ContextCompat.getColor(this, R.color.textPrimary)
        leftAxis.textSize = textSize
        leftAxis.axisMinimum = 0f

        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false
    }

    private fun updateProgressBar(remaining: Double, percentage: Int) {
        val progressDrawable = progressBar.progressDrawable.mutate() as LayerDrawable
        val progressLayer = progressDrawable.findDrawableByLayerId(android.R.id.progress)

        if (remaining < 0) {
            progressBar.progress = progressBar.max
            progressLayer.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark), PorterDuff.Mode.SRC_IN)
        } else {
            progressBar.progress = percentage
            val green = (2.55 * percentage).toInt()
            val red = 255 - green
            progressLayer.colorFilter = PorterDuffColorFilter(Color.rgb(red, green, 0), PorterDuff.Mode.SRC_IN)
        }
    }
}