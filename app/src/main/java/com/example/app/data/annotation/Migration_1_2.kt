package com.example.app.data.annotation

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `attachments` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `annotationId` INTEGER NOT NULL,
                `type` TEXT NOT NULL,
                `displayName` TEXT NOT NULL,
                `data` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`annotationId`) REFERENCES `annotations`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_attachments_annotationId` ON `attachments` (`annotationId`)"
        )
    }
}
