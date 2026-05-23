package com.example.app.data.annotation

data class PendingAttachment(
    val type: AttachmentType,
    val displayName: String,
    val urlOrPath: String
)
