package com.xaymaca.sit.ui.tickle

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xaymaca.sit.data.model.TickleFrequency
import com.xaymaca.sit.data.model.TickleReminder
import com.xaymaca.sit.data.model.TickleStatus
import com.xaymaca.sit.data.repository.ContactRepository
import com.xaymaca.sit.data.repository.TickleRepository
import com.xaymaca.sit.service.TickleScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TickleViewModel @Inject constructor(
    private val tickleRepository: TickleRepository,
    private val contactRepository: ContactRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val allReminders: StateFlow<List<TickleReminder>> = tickleRepository
        .getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueReminders: StateFlow<List<TickleReminder>> = allReminders
        .map { list ->
            val now = System.currentTimeMillis()
            list.filter { it.status == TickleStatus.ACTIVE.name && it.nextDueDate <= now }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingReminders: StateFlow<List<TickleReminder>> = allReminders
        .map { list ->
            val now = System.currentTimeMillis()
            list.filter { it.status == TickleStatus.ACTIVE.name && it.nextDueDate > now }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val snoozedReminders: StateFlow<List<TickleReminder>> = allReminders
        .map { list -> list.filter { it.status == TickleStatus.SNOOZED.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markComplete(reminder: TickleReminder) {
        viewModelScope.launch {
            val nextDue = TickleScheduler.nextDueDate(
                from = System.currentTimeMillis(),
                frequency = reminder.frequency,
                customDays = reminder.customIntervalDays
            )
            tickleRepository.updateReminder(
                reminder.copy(
                    lastCompletedDate = System.currentTimeMillis(),
                    nextDueDate = nextDue,
                    status = TickleStatus.ACTIVE.name
                )
            )
        }
    }

    fun snooze(reminder: TickleReminder, days: Int = 7) {
        viewModelScope.launch {
            val snoozeUntil = System.currentTimeMillis() + days * 24L * 60 * 60 * 1000
            tickleRepository.updateReminder(
                reminder.copy(
                    nextDueDate = snoozeUntil,
                    status = TickleStatus.SNOOZED.name
                )
            )
        }
    }

    fun delete(reminder: TickleReminder) {
        viewModelScope.launch {
            TickleScheduler.cancelNotification(context, reminder.id)
            tickleRepository.deleteReminder(reminder)
        }
    }

    fun upsert(reminder: TickleReminder) {
        viewModelScope.launch {
            val id = tickleRepository.upsertReminder(reminder)
            val finalId = if (reminder.id == 0L) id else reminder.id
            // Determine contact name for notification
            val contactName = reminder.contactId?.let { cId ->
                contactRepository.getContactById(cId)?.fullName
            } ?: "your contact"
            TickleScheduler.scheduleNotification(
                context, finalId, contactName, reminder.note, reminder.nextDueDate
            )
            // Ensure daily worker is scheduled
            TickleScheduler.scheduleWorker(context)
        }
    }

    suspend fun getReminderById(id: Long): TickleReminder? = tickleRepository.getReminderById(id)
}
