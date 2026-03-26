package com.example.financialtracker.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financialtracker.R
import com.example.financialtracker.data.model.SearchResult
import com.example.financialtracker.data.model.SearchResultType

class SearchAdapter(
    private var searchResults: List<SearchResult>,
    private val onSearchResultClicked: (SearchResult) -> Unit
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_result_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchResult = searchResults[position]
        holder.bind(searchResult)
        holder.itemView.setOnClickListener { onSearchResultClicked(searchResult) }
    }

    override fun getItemCount() = searchResults.size

    fun updateData(newSearchResults: List<SearchResult>) {
        this.searchResults = newSearchResults
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTextView: TextView = view.findViewById(R.id.item_title)
        private val subtitleTextView: TextView = view.findViewById(R.id.item_subtitle)
        private val iconImageView: ImageView = view.findViewById(R.id.item_icon)
        private val iconContainer: FrameLayout = view.findViewById(R.id.iconContainer)

        fun bind(searchResult: SearchResult) {
            titleTextView.text = searchResult.title
            subtitleTextView.text = searchResult.subtitle
            
            val (iconRes, bgRes) = when (searchResult.type) {
                SearchResultType.FRIEND -> Pair(R.drawable.user, R.drawable.circle_avatar_bg)
                SearchResultType.INCOME -> Pair(R.drawable.profit, R.drawable.circle_icon_bg_green)
                SearchResultType.EXPENSE -> Pair(R.drawable.expenses, R.drawable.circle_icon_bg_red)
            }
            
            iconImageView.setImageResource(iconRes)
            iconContainer.setBackgroundResource(bgRes)
        }
    }
}