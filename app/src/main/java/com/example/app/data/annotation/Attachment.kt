package com.example.app.data.annotation

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class AttachmentType { LINK, PDF }

@Entity(
    tableName = "attachments",
    foreignKeys = [ForeignKey(
        entity = Annotation::class,
        parentColumns = ["id"],
        childColumns = ["annotationId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["annotationId"])]
)
data class Attachment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val annotationId: Long,
    val type: AttachmentType,
    val displayName: String,
    val data: String,
    val createdAt: Long
)

data class AnnotationWithAttachments(
    @Embedded val annotation: Annotation,
    @Relation(parentColumn = "id", entityColumn = "annotationId")
    val attachments: List<Attachment>
)
