package com.xaymaca.sit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_groups")
data class ContactGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val emoji: String = "👥",
    val createdAt: Long = System.currentTimeMillis()
)
