package com.example.app.ui.booklist

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
import com.example.app.data.model.Testament
import com.example.app.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookListViewModel(
    private val repository: BibleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val testament: Testament = Testament.valueOf(checkNotNull(savedStateHandle["testament"]))

    private val _uiState = MutableStateFlow<UiState<List<Book>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Book>>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { repository.getBooks() }
                .onSuccess { books ->
                    val filtered = when (testament) {
                        Testament.OLD -> books.filter { it.id <= 39 }
                        Testament.NEW -> books.filter { it.id > 39 }
                    }
                    _uiState.value = UiState.Success(filtered)
                }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "오류가 발생했습니다") }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App
                BookListViewModel(app.repository, createSavedStateHandle())
            }
        }
    }
}
