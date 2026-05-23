package com.example.app.ui.navigation

import com.example.app.data.model.Testament

sealed class Screen(val route: String) {
    data object TestamentSelection : Screen("testament_selection")
    data object BookList : Screen("book_list/{testament}") {
        fun createRoute(testament: Testament) = "book_list/${testament.name}"
    }
    data object ChapterList : Screen("chapter_list/{bookId}") {
        fun createRoute(bookId: Int) = "chapter_list/$bookId"
    }
    data object Verse : Screen("verse/{bookId}/{chapterNumber}") {
        fun createRoute(bookId: Int, chapterNumber: Int) = "verse/$bookId/$chapterNumber"
    }
}
