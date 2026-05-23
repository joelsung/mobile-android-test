package com.example.app.data.annotation

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnotationDao {

    @Upsert
    suspend fun upsert(annotation: Annotation)

    @Query("DELETE FROM annotations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM annotations WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse ORDER BY createdAt ASC")
    fun getByVerse(bookId: Int, chapter: Int, verse: Int): Flow<List<Annotation>>

    @Query("SELECT DISTINCT verse FROM annotations WHERE bookId = :bookId AND chapter = :chapter")
    fun getAnnotatedVersesInChapter(bookId: Int, chapter: Int): Flow<List<Int>>

    @Query("SELECT * FROM annotations ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<Annotation>>
}
