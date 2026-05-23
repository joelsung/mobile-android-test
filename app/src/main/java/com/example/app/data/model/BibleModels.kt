package com.example.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BibleData(val books: List<Book>)

@Serializable
data class Book(
    val id: Int,
    val nameKor: String,
    val nameEng: String,
    val chapters: List<Chapter>
)

@Serializable
data class Chapter(
    val number: Int,
    val verses: List<Verse>
)

@Serializable
data class Verse(
    val number: Int,
    val text: String
)
