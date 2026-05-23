package com.example.app

import android.app.Application
import com.example.app.data.BibleRepository
import com.example.app.data.annotation.AnnotationRepository
import com.example.app.data.annotation.BibleAnnotationDatabase

class App : Application() {
    val repository by lazy { BibleRepository(this) }
    val annotationRepository by lazy {
        AnnotationRepository(BibleAnnotationDatabase.getInstance(this))
    }
}
