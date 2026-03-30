package com.example.financialtracker.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.financialtracker.R

class TransactionAdapter(
    private var transactions: List<Triple<String, Double, String>>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.transactionIcon)
        val iconContainer: View = view.findViewById(R.id.transactionIconContainer)
        val title: TextView = view.findViewById(R.id.transactionTitle)
        val date: TextView = view.findViewById(R.id.transactionDate)
        val amount: TextView = view.findViewById(R.id.transactionAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val (category, amount, date) = transactions[position]
        val context = holder.itemView.context

        holder.title.text = category
        holder.date.text = date
        holder.amount.text = context.getString(R.string.amount_format, amount)

        if (category.startsWith("Income")) {
            holder.icon.setImageResource(R.drawable.profit)
            holder.iconContainer.setBackgroundResource(R.drawable.circle_icon_bg_green)
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.income_green))
        } else {
            holder.icon.setImageResource(R.drawable.expenses)
            holder.iconContainer.setBackgroundResource(R.drawable.circle_icon_bg_red)
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.expense_amount))
        }
    }

    override fun getItemCount() = transactions.size

    fun updateData(newTransactions: List<Triple<String, Double, String>>) {
        val diffResult = DiffUtil.calculateDiff(TransactionDiffCallback(transactions, newTransactions))
        transactions = newTransactions
        diffResult.dispatchUpdatesTo(this)
    }

    class TransactionDiffCallback(
        private val oldList: List<Triple<String, Double, String>>,
        private val newList: List<Triple<String, Double, String>>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}