package com.example.app.data.annotation

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

suspend fun copyPdfToInternal(context: Context, uri: Uri, displayName: String): String =
    withContext(Dispatchers.IO) {
        val pdfsDir = File(context.filesDir, "pdfs").also { it.mkdirs() }
        val dest = File(pdfsDir, "${UUID.randomUUID()}.pdf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Cannot open PDF stream for: $displayName")
        dest.absolutePath
    }
