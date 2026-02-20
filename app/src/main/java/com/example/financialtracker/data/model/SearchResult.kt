package com.example.financialtracker.data.model

enum class SearchResultType {
    FRIEND, INCOME, EXPENSE
}

data class SearchResult(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: SearchResultType
)
