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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.financialtracker.R
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
    private lateinit var tableLayout: TableLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewAmount: TextView
    private lateinit var viewModel: MainViewModel
    private lateinit var setBudgetButton: Button
    private lateinit var lineChart: LineChart
    private lateinit var friendsLinearLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_main, findViewById(R.id.content_container), true)

        tableLayout = findViewById(R.id.tableLayout)
        progressBar = findViewById(R.id.progressBudget)
        textViewAmount = findViewById(R.id.remainingBudget)
        setBudgetButton = findViewById(R.id.setBudgetButton)
        lineChart = findViewById(R.id.lineChart)
        friendsLinearLayout = findViewById(R.id.friendsLinearLayout)

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
            textViewAmount.text = String.format(Locale.US, "$%.2f", remaining)
            val percentage = viewModel.calculateBudgetPercentage(remaining)
            updateProgressBar(remaining, percentage)
        }

        viewModel.allTransactions.observe(this) { transactions ->
            populateTable(transactions)
        }

        viewModel.chartData.observe(this) { entries ->
            if (entries.isNotEmpty()) {
                setupChart(lineChart)
                val dataSet = LineDataSet(entries, "Daily Spending")
                dataSet.color = ContextCompat.getColor(this, R.color.primary)
                dataSet.valueTextColor = ContextCompat.getColor(this, R.color.on_primary)
                dataSet.setCircleColor(ContextCompat.getColor(this, R.color.primary))
                dataSet.circleHoleColor = ContextCompat.getColor(this, R.color.primary)
                dataSet.setDrawFilled(true)
                dataSet.fillColor = ContextCompat.getColor(this, R.color.primary)
                dataSet.fillAlpha = 100

                val lineData = LineData(dataSet)
                lineChart.data = lineData
                lineChart.invalidate() // refresh
            }
        }

        viewModel.friendsData.observe(this) { friends ->
            populateFriends(friends)
        }
    }

    private fun populateFriends(friends: List<Triple<String, String, Int>>) {
        friendsLinearLayout.removeAllViews()
        val inflater = LayoutInflater.from(this)
        for ((name, profileImageUrl, progress) in friends) {
            val friendView = inflater.inflate(R.layout.item_friend_progress, friendsLinearLayout, false)
            val friendProgressBar = friendView.findViewById<ProgressBar>(R.id.friendProgressBar)
            val friendNameTextView = friendView.findViewById<TextView>(R.id.friendNameTextView)
            val friendProfileImageView = friendView.findViewById<ImageView>(R.id.friendProfileImageView)

            friendProgressBar.progress = progress
            friendNameTextView.text = name

            if (profileImageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .into(friendProfileImageView)
            } else {
                friendProfileImageView.setImageResource(R.drawable.user)
            }

            friendsLinearLayout.addView(friendView)
        }
    }

    private fun setupChart(chart: LineChart) {
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = ContextCompat.getColor(this, R.color.textPrimary)
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.textColor = ContextCompat.getColor(this, R.color.textPrimary)
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

    private fun populateTable(transactions: List<Triple<String, Double, String>>) {
        val header = tableLayout.getChildAt(0)
        tableLayout.removeAllViews()
        tableLayout.addView(header)

        transactions.forEach { (category, amount, date) ->
            val row = LayoutInflater.from(this).inflate(R.layout.table_row_layout, tableLayout, false) as TableRow

            val iconTextView = row.findViewById<TextView>(R.id.column_icon)
            val typeTextView = row.findViewById<TextView>(R.id.column_type)
            val amountTextView = row.findViewById<TextView>(R.id.column_amount)
            val dateTextView = row.findViewById<TextView>(R.id.column_date)

            if (category.startsWith("Income")) {
                iconTextView.text = "💰"
            } else {
                iconTextView.text = "💸"
            }

            typeTextView.text = category
            amountTextView.text = String.format(Locale.US, "$%.2f", amount)
            dateTextView.text = date

            tableLayout.addView(row)
        }
    }
}