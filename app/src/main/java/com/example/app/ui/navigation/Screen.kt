package com.example.app.ui.navigation

import com.example.app.data.model.Testament

sealed class Screen(val route: String) {
    data object TestamentSelection : Screen("testament_selection")
    data object Search : Screen("search")
    data object BookList : Screen("book_list/{testament}") {
        fun createRoute(testament: Testament) = "book_list/${testament.name}"
    }
    data object ChapterList : Screen("chapter_list/{bookId}") {
        fun createRoute(bookId: Int) = "chapter_list/$bookId"
    }
    data object Verse : Screen("verse/{bookId}/{chapterNumber}?verseAnchor={verseAnchor}") {
        fun createRoute(bookId: Int, chapterNumber: Int, verseAnchor: Int? = null): String {
            val base = "verse/$bookId/$chapterNumber"
            return if (verseAnchor != null) "$base?verseAnchor=$verseAnchor" else base
        }
    }
}
