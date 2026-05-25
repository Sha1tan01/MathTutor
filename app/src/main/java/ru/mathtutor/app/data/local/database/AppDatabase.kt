package ru.mathtutor.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.mathtutor.app.data.local.dao.ChatMessageDao
import ru.mathtutor.app.data.local.dao.UserProgressDao
import ru.mathtutor.app.data.local.entity.ChatMessageEntity
import ru.mathtutor.app.data.local.entity.UserProgressEntity

@Database(
    entities = [ChatMessageEntity::class, UserProgressEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun userProgressDao(): UserProgressDao
}
