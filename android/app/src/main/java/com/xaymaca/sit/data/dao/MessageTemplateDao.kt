package com.xaymaca.sit.data.dao

import androidx.room.*
import com.xaymaca.sit.data.model.MessageTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageTemplateDao {

    @Query("SELECT * FROM message_templates ORDER BY createdAt DESC")
    fun getAll(): Flow<List<MessageTemplate>>

    @Query("SELECT * FROM message_templates WHERE id = :id")
    suspend fun getById(id: Long): MessageTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: MessageTemplate): Long

    @Update
    suspend fun update(template: MessageTemplate)

    @Delete
    suspend fun delete(template: MessageTemplate)
}
