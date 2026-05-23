package com.example.app.data.crossref

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CrossReferenceRepository(private val context: Context) {

    private val _index = MutableStateFlow<Map<Long, List<CrossReference>>>(emptyMap())

    fun getReferencesFor(bookId: Int, chapter: Int, verse: Int): Flow<List<CrossReference>> =
        _index.map { map -> map[verseKey(bookId, chapter, verse)] ?: emptyList() }

    fun getVersesWithCrossRefsInChapter(bookId: Int, chapter: Int): Flow<Set<Int>> =
        _index.map { map ->
            val chapterBase = bookId.toLong() * 1_000_000L + chapter.toLong() * 1_000L
            (1..176).filterTo(mutableSetOf()) { verse -> map.containsKey(chapterBase + verse) }
        }

    suspend fun load() = withContext(Dispatchers.IO) {
        if (_index.value.isNotEmpty()) return@withContext
        val tmp = HashMap<Long, MutableList<CrossReference>>(400_000)
        context.assets.open("cross_references.txt").bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val tab1 = line.indexOf('\t')
                if (tab1 < 0) return@forEach
                val tab2 = line.indexOf('\t', tab1 + 1)
                if (tab2 < 0) return@forEach
                val tab3 = line.indexOf('\t', tab2 + 1)

                val fromStr = line.substring(0, tab1)
                val toStr = line.substring(tab1 + 1, tab2)
                val votesStr = if (tab3 > 0) line.substring(tab2 + 1, tab3) else line.substring(tab2 + 1)

                val from = parseRef(fromStr) ?: return@forEach
                val toRange = parseRange(toStr) ?: return@forEach
                val votes = votesStr.trim().toIntOrNull() ?: return@forEach

                val cr = CrossReference(
                    fromBookId = from.first,
                    fromChapter = from.second,
                    fromVerse = from.third,
                    toBookId = toRange.first.first,
                    toChapter = toRange.first.second,
                    toVerseStart = toRange.first.third,
                    toVerseEnd = toRange.second,
                    votes = votes
                )
                tmp.getOrPut(verseKey(from.first, from.second, from.third)) { mutableListOf() }.add(cr)
            }
        }
        _index.value = tmp.mapValues { (_, list) -> list.sortedByDescending { it.votes } }
    }

    private fun parseRef(s: String): Triple<Int, Int, Int>? {
        val dot1 = s.indexOf('.')
        if (dot1 < 0) return null
        val dot2 = s.indexOf('.', dot1 + 1)
        if (dot2 < 0) return null
        val bookId = osisToBookId[s.substring(0, dot1)] ?: return null
        val chapter = s.substring(dot1 + 1, dot2).toIntOrNull() ?: return null
        val verse = s.substring(dot2 + 1).toIntOrNull() ?: return null
        return Triple(bookId, chapter, verse)
    }

    // Returns Pair<Triple<book, chapter, verseStart>, verseEnd>
    private fun parseRange(s: String): Pair<Triple<Int, Int, Int>, Int>? {
        val dashIdx = s.indexOf('-')
        if (dashIdx < 0) {
            val ref = parseRef(s) ?: return null
            return Pair(ref, ref.third)
        }
        val startRef = parseRef(s.substring(0, dashIdx)) ?: return null
        val endRef = parseRef(s.substring(dashIdx + 1))
        val verseEnd = if (endRef != null
            && endRef.first == startRef.first
            && endRef.second == startRef.second
        ) endRef.third else startRef.third
        return Pair(startRef, verseEnd)
    }

    private fun verseKey(bookId: Int, chapter: Int, verse: Int): Long =
        bookId.toLong() * 1_000_000L + chapter.toLong() * 1_000L + verse.toLong()
}
