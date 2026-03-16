package com.xaymaca.sit.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.xaymaca.sit.SITApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences(SITApp.PREFS_NAME, Context.MODE_PRIVATE)

    private val _sendDirectly = MutableStateFlow(
        prefs.getBoolean(SITApp.KEY_SEND_SMS_DIRECTLY, false)
    )
    val sendDirectly: StateFlow<Boolean> = _sendDirectly

    fun toggleSendDirectly() {
        val newValue = !_sendDirectly.value
        _sendDirectly.value = newValue
        prefs.edit().putBoolean(SITApp.KEY_SEND_SMS_DIRECTLY, newValue).apply()
    }
}
