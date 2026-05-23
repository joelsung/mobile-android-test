package com.example.app.data.annotation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class AnnotationRepository(private val db: BibleAnnotationDatabase) {

    private val annotationDao = db.annotationDao()
    private val attachmentDao = db.attachmentDao()

    // ── Annotation ────────────────────────────────────────────────────────────

    suspend fun upsert(annotation: Annotation) = annotationDao.upsert(annotation)

    suspend fun deleteAnnotationById(id: Long) {
        attachmentDao.getPdfsByAnnotationId(id).forEach { File(it.data).delete() }
        annotationDao.deleteById(id)
    }

    fun getByVerse(bookId: Int, chapter: Int, verse: Int): Flow<List<Annotation>> =
        annotationDao.getByVerse(bookId, chapter, verse)

    fun getAnnotatedVersesInChapter(bookId: Int, chapter: Int): Flow<Set<Int>> =
        annotationDao.getAnnotatedVersesInChapter(bookId, chapter).map { it.toSet() }

    fun getAll(): Flow<List<Annotation>> = annotationDao.getAll()

    // ── Attachment ────────────────────────────────────────────────────────────

    suspend fun deleteAttachment(attachment: Attachment) {
        if (attachment.type == AttachmentType.PDF) File(attachment.data).delete()
        attachmentDao.deleteById(attachment.id)
    }

    // ── Combined ──────────────────────────────────────────────────────────────

    fun getAnnotationWithAttachments(
        bookId: Int,
        chapter: Int,
        verse: Int
    ): Flow<List<AnnotationWithAttachments>> =
        annotationDao.getWithAttachmentsByVerse(bookId, chapter, verse)

    suspend fun saveAnnotationWithAttachments(
        annotation: Annotation,
        pendingAttachments: List<PendingAttachment>
    ) {
        db.withTransaction {
            val now = System.currentTimeMillis()
            val annotationId = if (annotation.id == 0L) {
                annotationDao.insert(annotation)
            } else {
                annotationDao.upsert(annotation)
                annotation.id
            }
            pendingAttachments.forEach { pending ->
                attachmentDao.insert(
                    Attachment(
                        annotationId = annotationId,
                        type = pending.type,
                        displayName = pending.displayName,
                        data = pending.urlOrPath,
                        createdAt = now
                    )
                )
            }
        }
    }
}
