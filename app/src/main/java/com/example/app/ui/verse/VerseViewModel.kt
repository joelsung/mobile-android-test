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
import com.example.app.data.annotation.Annotation
import com.example.app.data.annotation.AnnotationRepository
import com.example.app.data.annotation.AnnotationWithAttachments
import com.example.app.data.annotation.Attachment
import com.example.app.data.annotation.AttachmentType
import com.example.app.data.annotation.PendingAttachment
import com.example.app.data.model.Chapter
import com.example.app.ui.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VerseViewModel(
    private val bibleRepository: BibleRepository,
    private val annotationRepository: AnnotationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val bookId: Int = checkNotNull(savedStateHandle["bookId"])
    val chapterNumber: Int = checkNotNull(savedStateHandle["chapterNumber"])

    private val _uiState = MutableStateFlow<UiState<Pair<String, Chapter>>>(UiState.Loading)
    val uiState: StateFlow<UiState<Pair<String, Chapter>>> = _uiState.asStateFlow()

    val annotatedVerses: StateFlow<Set<Int>> = annotationRepository
        .getAnnotatedVersesInChapter(bookId, chapterNumber)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _selectedVerse = MutableStateFlow<Int?>(null)
    val selectedVerse: StateFlow<Int?> = _selectedVerse.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedVerseAnnotations: StateFlow<List<AnnotationWithAttachments>> = _selectedVerse
        .flatMapLatest { verseNum ->
            if (verseNum == null) flowOf(emptyList())
            else annotationRepository.getAnnotationWithAttachments(bookId, chapterNumber, verseNum)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _pendingAttachments = MutableStateFlow<List<PendingAttachment>>(emptyList())
    val pendingAttachments: StateFlow<List<PendingAttachment>> = _pendingAttachments.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                val book = bibleRepository.getBook(bookId)
                val chapter = bibleRepository.getChapter(bookId, chapterNumber)
                checkNotNull(book) to checkNotNull(chapter)
            }
                .onSuccess { (book, chapter) ->
                    _uiState.value = UiState.Success(book.nameKor to chapter)
                }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "오류가 발생했습니다") }
        }
    }

    fun selectVerse(verseNum: Int) { _selectedVerse.value = verseNum }

    fun dismissBottomSheet() {
        _selectedVerse.value = null
        _pendingAttachments.value = emptyList()
    }

    fun addPendingLink(displayName: String, url: String) {
        _pendingAttachments.update { it + PendingAttachment(AttachmentType.LINK, displayName, url) }
    }

    fun addPendingPdf(displayName: String, internalPath: String) {
        _pendingAttachments.update { it + PendingAttachment(AttachmentType.PDF, displayName, internalPath) }
    }

    fun removePendingAttachment(index: Int) {
        _pendingAttachments.update { list -> list.filterIndexed { i, _ -> i != index } }
    }

    fun saveAnnotation(verseNum: Int, content: String, existing: AnnotationWithAttachments?) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val annotation = existing?.annotation?.copy(content = content, updatedAt = now)
                ?: Annotation(
                    bookId = bookId,
                    chapter = chapterNumber,
                    verse = verseNum,
                    content = content,
                    createdAt = now,
                    updatedAt = now
                )
            annotationRepository.saveAnnotationWithAttachments(annotation, _pendingAttachments.value)
            _pendingAttachments.value = emptyList()
        }
    }

    fun deleteAnnotation(id: Long) {
        viewModelScope.launch { annotationRepository.deleteAnnotationById(id) }
    }

    fun deleteAttachment(attachment: Attachment) {
        viewModelScope.launch { annotationRepository.deleteAttachment(attachment) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App
                VerseViewModel(app.repository, app.annotationRepository, createSavedStateHandle())
            }
        }
    }
}
