package com.example.app.data.annotation

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Annotation::class], version = 1, exportSchema = false)
abstract class BibleAnnotationDatabase : RoomDatabase() {

    abstract fun annotationDao(): AnnotationDao

    companion object {
        @Volatile private var instance: BibleAnnotationDatabase? = null

        fun getInstance(context: Context): BibleAnnotationDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BibleAnnotationDatabase::class.java,
                    "bible_annotations.db"
                ).build().also { instance = it }
            }
    }
}
