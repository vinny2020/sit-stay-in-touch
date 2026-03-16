package com.xaymaca.sit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickle_reminders")
data class TickleReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: Long? = null,
    val groupId: Long? = null,
    val note: String = "",
    val frequency: String = TickleFrequency.MONTHLY.name,
    val customIntervalDays: Int? = null,
    val startDate: Long = System.currentTimeMillis(),
    val nextDueDate: Long = System.currentTimeMillis(),
    val lastCompletedDate: Long? = null,
    val status: String = TickleStatus.ACTIVE.name,
    val createdAt: Long = System.currentTimeMillis()
)
