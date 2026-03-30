package com.example.financialtracker.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financialtracker.R
import com.example.financialtracker.data.model.Income
import java.text.SimpleDateFormat
import java.util.Locale

class IncomeAdapter(
    private var incomes: List<Income>,
    private val onItemClick: (Income) -> Unit
) : RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>() {

    inner class IncomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.incomeIcon)
        val source: TextView = view.findViewById(R.id.incomeSource)
        val date: TextView = view.findViewById(R.id.incomeDate)
        val amount: TextView = view.findViewById(R.id.incomeAmount)

        init {
            view.setOnClickListener {
                onItemClick(incomes[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_income, parent, false)
        return IncomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        val income = incomes[position]
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        holder.source.text = income.source
        holder.date.text = income.date?.toDate()?.let { formatter.format(it) } ?: ""
        holder.amount.text = holder.itemView.context.getString(R.string.amount_format, income.amount)

        val iconResource = when (income.source) {
            "Salary" -> R.drawable.profit
            "Freelance" -> R.drawable.id_card
            "Investment" -> R.drawable.profit
            "Gift" -> R.drawable.profit
            else -> R.drawable.profit
        }
        holder.icon.setImageResource(iconResource)
    }

    override fun getItemCount(): Int = incomes.size

    fun updateIncomes(newIncomes: List<Income>) {
        incomes = newIncomes
        notifyDataSetChanged()
    }
}