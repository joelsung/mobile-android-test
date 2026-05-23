package com.example.app.data.search

data class Reference(val bookId: Int, val chapter: Int, val verse: Int?)

object ReferenceParser {

    // Flat list of (alias, bookId) sorted by alias length descending — longest match wins
    private val aliasIndex: List<Pair<String, Int>> = BOOK_ALIASES
        .flatMap { (bookId, aliases) -> aliases.map { it to bookId } }
        .sortedByDescending { it.first.length }

    fun parse(query: String): Reference? {
        val q = query.trim().replace(Regex("\\s+"), " ")
        if (q.isEmpty()) return null

        var bookId: Int? = null
        var remainder = ""

        for ((alias, id) in aliasIndex) {
            if (q.startsWith(alias)) {
                bookId = id
                remainder = q.removePrefix(alias).trim()
                break
            }
        }
        if (bookId == null || remainder.isEmpty()) return null

        // "5:1" or "5 : 1"
        Regex("""^(\d+)\s*:\s*(\d+)""").find(remainder)?.let {
            return Reference(bookId, it.groupValues[1].toInt(), it.groupValues[2].toInt())
        }
        // "5장 1절" or "5장1절"
        Regex("""^(\d+)\s*장\s*(\d+)\s*절""").find(remainder)?.let {
            return Reference(bookId, it.groupValues[1].toInt(), it.groupValues[2].toInt())
        }
        // "5장"
        Regex("""^(\d+)\s*장""").find(remainder)?.let {
            return Reference(bookId, it.groupValues[1].toInt(), null)
        }
        // "5"
        Regex("""^(\d+)""").find(remainder)?.let {
            return Reference(bookId, it.groupValues[1].toInt(), null)
        }
        return null
    }
}
