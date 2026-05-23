package com.example.app.ui.navigation

sealed class Screen(val route: String) {
    data object BookList : Screen("book_list")
    data object ChapterList : Screen("chapter_list/{bookId}") {
        fun createRoute(bookId: Int) = "chapter_list/$bookId"
    }
    data object Verse : Screen("verse/{bookId}/{chapterNumber}") {
        fun createRoute(bookId: Int, chapterNumber: Int) = "verse/$bookId/$chapterNumber"
    }
}
