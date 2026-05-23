package com.example.app.data.annotation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnotationDao {

    @Insert
    suspend fun insert(annotation: Annotation): Long

    @Upsert
    suspend fun upsert(annotation: Annotation)

    @Query("DELETE FROM annotations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM annotations WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse ORDER BY createdAt ASC")
    fun getByVerse(bookId: Int, chapter: Int, verse: Int): Flow<List<Annotation>>

    @Transaction
    @Query("SELECT * FROM annotations WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse ORDER BY createdAt ASC")
    fun getWithAttachmentsByVerse(bookId: Int, chapter: Int, verse: Int): Flow<List<AnnotationWithAttachments>>

    @Query("SELECT DISTINCT verse FROM annotations WHERE bookId = :bookId AND chapter = :chapter")
    fun getAnnotatedVersesInChapter(bookId: Int, chapter: Int): Flow<List<Int>>

    @Query("SELECT * FROM annotations ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<Annotation>>
}
