package com.example.app

import android.app.Application
import com.example.app.data.BibleRepository
import com.example.app.data.annotation.AnnotationRepository
import com.example.app.data.annotation.BibleAnnotationDatabase
import com.example.app.data.crossref.CrossReferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class App : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val repository by lazy { BibleRepository(this) }
    val annotationRepository by lazy {
        AnnotationRepository(BibleAnnotationDatabase.getInstance(this))
    }
    val crossReferenceRepository by lazy { CrossReferenceRepository(this) }

    override fun onCreate() {
        super.onCreate()
        appScope.launch { crossReferenceRepository.load() }
    }
}
