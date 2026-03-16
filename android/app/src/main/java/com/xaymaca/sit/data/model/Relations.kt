package com.xaymaca.sit.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ContactWithGroups(
    @Embedded val contact: Contact,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ContactGroupCrossRef::class,
            parentColumn = "contactId",
            entityColumn = "groupId"
        )
    )
    val groups: List<ContactGroup>
)

data class GroupWithContacts(
    @Embedded val group: ContactGroup,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ContactGroupCrossRef::class,
            parentColumn = "groupId",
            entityColumn = "contactId"
        )
    )
    val contacts: List<Contact>
)
