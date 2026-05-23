package com.example.app.data.annotation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Insert
    suspend fun insert(attachment: Attachment): Long

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM attachments WHERE annotationId = :annotationId")
    suspend fun deleteByAnnotationId(annotationId: Long)

    @Query("SELECT * FROM attachments WHERE annotationId = :annotationId ORDER BY createdAt ASC")
    fun getByAnnotationId(annotationId: Long): Flow<List<Attachment>>

    @Query("SELECT * FROM attachments WHERE annotationId = :annotationId AND type = 'PDF'")
    suspend fun getPdfsByAnnotationId(annotationId: Long): List<Attachment>
}
