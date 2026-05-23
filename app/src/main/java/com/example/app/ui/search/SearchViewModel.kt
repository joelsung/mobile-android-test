package com.example.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.app.App
import com.example.app.data.BibleRepository
import com.example.app.data.search.Reference
import com.example.app.data.search.ReferenceParser
import com.example.app.data.search.SearchResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class)
class SearchViewModel(private val repository: BibleRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val debouncedQuery = _query
        .debounce(300)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val referenceMatch: StateFlow<Reference?> = debouncedQuery
        .map { q -> if (q.isNotBlank()) ReferenceParser.parse(q) else null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val textResults: StateFlow<List<SearchResult>> = debouncedQuery
        .map { q -> if (q.trim().length >= 2) search(q.trim()) else emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(q: String) { _query.value = q }

    private suspend fun search(query: String): List<SearchResult> {
        val normalised = query.replace(Regex("\\s+"), " ")
        val results = mutableListOf<SearchResult>()
        for (book in repository.getBooks()) {
            for (chapter in book.chapters) {
                for (verse in chapter.verses) {
                    if (verse.text.contains(normalised, ignoreCase = true)) {
                        results.add(
                            SearchResult(
                                bookId = book.id,
                                bookNameKor = book.nameKor,
                                chapter = chapter.number,
                                verse = verse.number,
                                text = verse.text
                            )
                        )
                        if (results.size >= 100) return results
                    }
                }
            }
        }
        return results
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App
                SearchViewModel(app.repository)
            }
        }
    }
}
