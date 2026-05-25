package ru.mathtutor.app.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.mathtutor.app.data.local.entity.ChatMessageEntity
import ru.mathtutor.app.data.local.entity.UserProgressEntity

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()
}

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress")
    fun getAllProgress(): Flow<List<UserProgressEntity>>

    @Query("SELECT * FROM user_progress WHERE topicId = :topicId")
    fun getProgress(topicId: String): Flow<UserProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: UserProgressEntity)

    @Query("SELECT COUNT(*) FROM user_progress WHERE status = 'DONE'")
    suspend fun countDone(): Int

    @Query("SELECT COUNT(*) FROM user_progress")
    suspend fun countAll(): Int

    @Query("SELECT * FROM user_progress ORDER BY lastOpenedAt DESC LIMIT 1")
    suspend fun getLastOpened(): UserProgressEntity?
}
