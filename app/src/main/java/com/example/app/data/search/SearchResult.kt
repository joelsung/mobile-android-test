package com.example.app.data.search

data class SearchResult(
    val bookId: Int,
    val bookNameKor: String,
    val chapter: Int,
    val verse: Int,
    val text: String
)
