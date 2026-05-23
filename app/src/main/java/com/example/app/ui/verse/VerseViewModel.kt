package com.example.app.ui.verse

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.app.App
import com.example.app.data.BibleRepository
import com.example.app.data.model.Chapter
import com.example.app.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VerseViewModel(
    private val repository: BibleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: Int = checkNotNull(savedStateHandle["bookId"])
    private val chapterNumber: Int = checkNotNull(savedStateHandle["chapterNumber"])

    private val _uiState = MutableStateFlow<UiState<Pair<String, Chapter>>>(UiState.Loading)
    val uiState: StateFlow<UiState<Pair<String, Chapter>>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                val book = repository.getBook(bookId)
                val chapter = repository.getChapter(bookId, chapterNumber)
                checkNotNull(book) to checkNotNull(chapter)
            }
                .onSuccess { (book, chapter) ->
                    _uiState.value = UiState.Success(book.nameKor to chapter)
                }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "오류가 발생했습니다") }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App
                VerseViewModel(app.repository, createSavedStateHandle())
            }
        }
    }
}
