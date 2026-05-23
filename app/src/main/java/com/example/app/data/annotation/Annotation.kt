package com.example.app.data.annotation

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "annotations",
    indices = [Index(value = ["bookId", "chapter", "verse"])]
)
data class Annotation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
