package com.example.app.data.annotation

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(
    entities = [Annotation::class, Attachment::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(AttachmentTypeConverter::class)
abstract class BibleAnnotationDatabase : RoomDatabase() {

    abstract fun annotationDao(): AnnotationDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        @Volatile private var instance: BibleAnnotationDatabase? = null

        fun getInstance(context: Context): BibleAnnotationDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BibleAnnotationDatabase::class.java,
                    "bible_annotations.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}

class AttachmentTypeConverter {
    @TypeConverter fun fromType(type: AttachmentType): String = type.name
    @TypeConverter fun toType(value: String): AttachmentType = AttachmentType.valueOf(value)
}
