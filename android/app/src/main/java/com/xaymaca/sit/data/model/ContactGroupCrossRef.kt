package com.xaymaca.sit.data.model

import androidx.room.Entity

@Entity(
    tableName = "contact_group_cross_ref",
    primaryKeys = ["contactId", "groupId"]
)
data class ContactGroupCrossRef(
    val contactId: Long,
    val groupId: Long
)
