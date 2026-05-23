package com.example.app.data

import android.content.Context
import com.example.app.data.model.BibleData
import com.example.app.data.model.Book
import com.example.app.data.model.Chapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class BibleRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    @Volatile private var cache: BibleData? = null

    private suspend fun getBibleData(): BibleData {
        return cache ?: withContext(Dispatchers.IO) {
            cache ?: run {
                val text = context.assets.open("bible.json").bufferedReader().readText()
                json.decodeFromString<BibleData>(text).also { cache = it }
            }
        }
    }

    suspend fun getBooks(): List<Book> = getBibleData().books

    suspend fun getBook(bookId: Int): Book? = getBibleData().books.find { it.id == bookId }

    suspend fun getChapter(bookId: Int, chapterNumber: Int): Chapter? =
        getBook(bookId)?.chapters?.find { it.number == chapterNumber }
}
