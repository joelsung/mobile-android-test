package com.example.app.ui.chapterlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.app.App
import com.example.app.data.BibleRepository
import com.example.app.data.model.Book
import com.example.app.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChapterListViewModel(
    private val repository: BibleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: Int = checkNotNull(savedStateHandle["bookId"])

    private val _uiState = MutableStateFlow<UiState<Book>>(UiState.Loading)
    val uiState: StateFlow<UiState<Book>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { repository.getBook(bookId) }
                .onSuccess { book ->
                    if (book != null) _uiState.value = UiState.Success(book)
                    else _uiState.value = UiState.Error("책을 찾을 수 없습니다")
                }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "오류가 발생했습니다") }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App
                ChapterListViewModel(app.repository, createSavedStateHandle())
            }
        }
    }
}
