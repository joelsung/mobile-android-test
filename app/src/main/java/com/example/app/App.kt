package com.example.app

import android.app.Application
import com.example.app.data.BibleRepository

class App : Application() {
    val repository by lazy { BibleRepository(this) }
}
