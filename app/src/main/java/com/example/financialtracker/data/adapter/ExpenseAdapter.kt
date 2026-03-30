package com.example.financialtracker.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financialtracker.R
import com.example.financialtracker.data.model.Expense
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val onItemClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ivExpenseIcon)
        val category: TextView = view.findViewById(R.id.tvExpenseCategory)
        val date: TextView = view.findViewById(R.id.tvExpenseDate)
        val amount: TextView = view.findViewById(R.id.tvExpenseAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        holder.category.text = expense.category
        holder.date.text = expense.date?.toDate()?.let { formatter.format(it) } ?: ""
        holder.amount.text = holder.itemView.context.getString(R.string.amount_format, expense.amount)

        val iconResource = when (expense.category) {
            "Groceries" -> R.drawable.expenses
            "Transport" -> R.drawable.expenses
            "Entertainment" -> R.drawable.expenses
            "Dining" -> R.drawable.expenses
            "Shopping" -> R.drawable.expenses
            "Gym Membership" -> R.drawable.expenses
            "Travel" -> R.drawable.expenses
            "Subscription" -> R.drawable.expenses
            "Education" -> R.drawable.expenses
            "Healthcare" -> R.drawable.expenses
            "Car Payment" -> R.drawable.expenses
            else -> R.drawable.expenses
        }
        holder.icon.setImageResource(iconResource)

        holder.itemView.setOnClickListener { onItemClick(expense) }
    }

    override fun getItemCount() = expenses.size

    fun updateData(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}