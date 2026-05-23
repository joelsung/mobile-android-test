package com.example.app.data.model

import kotlinx.serialization.Serializable

// Raw JSON models matching korrv.json schema
@Serializable
data class KorrvJson(val books: List<KorrvBook>)

@Serializable
data class KorrvBook(
    val name: String,
    val chapters: List<KorrvChapter>
)

@Serializable
data class KorrvChapter(
    val chapter: Int,
    val name: String,
    val verses: List<KorrvVerse>
)

@Serializable
data class KorrvVerse(
    val verse: Int,
    val chapter: Int,
    val name: String,
    val text: String
)

// Domain models
data class Book(
    val id: Int,
    val nameEng: String,
    val nameKor: String,
    val chapters: List<Chapter>
)

data class Chapter(
    val number: Int,
    val verses: List<Verse>
)

data class Verse(
    val number: Int,
    val text: String,
    val displayRef: String
)
