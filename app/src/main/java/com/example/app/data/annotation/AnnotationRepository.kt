package com.example.app.data.annotation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnnotationRepository(private val dao: AnnotationDao) {

    suspend fun upsert(annotation: Annotation) = dao.upsert(annotation)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    fun getByVerse(bookId: Int, chapter: Int, verse: Int): Flow<List<Annotation>> =
        dao.getByVerse(bookId, chapter, verse)

    fun getAnnotatedVersesInChapter(bookId: Int, chapter: Int): Flow<Set<Int>> =
        dao.getAnnotatedVersesInChapter(bookId, chapter).map { it.toSet() }

    fun getAll(): Flow<List<Annotation>> = dao.getAll()
}
