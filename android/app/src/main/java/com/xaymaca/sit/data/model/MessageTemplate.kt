package com.xaymaca.sit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_templates")
data class MessageTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val body: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
