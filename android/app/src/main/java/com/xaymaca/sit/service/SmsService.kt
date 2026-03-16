package com.xaymaca.sit.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsService @Inject constructor() {

    /**
     * Sends an SMS directly via SmsManager (requires SEND_SMS permission).
     * Returns true on success, false on failure.
     */
    fun sendSms(context: Context, phoneNumber: String, message: String): Boolean {
        return try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val parts = smsManager.divideMessage(message)
            if (parts.size == 1) {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            } else {
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Creates an Intent to open the default SMS app pre-filled with recipients and message.
     * This is the fallback when SEND_SMS permission is not granted.
     * For a single recipient uses sms: URI; for multiple uses smsto: with semicolon-separated numbers.
     */
    fun sendSmsIntent(context: Context, phoneNumbers: List<String>, message: String): Intent {
        return if (phoneNumbers.size == 1) {
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${phoneNumbers.first()}")
                putExtra("sms_body", message)
            }
        } else {
            val numbers = phoneNumbers.joinToString(";")
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$numbers")
                putExtra("sms_body", message)
            }
        }
    }

    /**
     * Sends to multiple recipients directly via SmsManager.
     * Returns a map of phoneNumber -> success/failure.
     */
    fun sendSmsToMany(
        context: Context,
        phoneNumbers: List<String>,
        message: String
    ): Map<String, Boolean> {
        return phoneNumbers.associateWith { number ->
            sendSms(context, number, message)
        }
    }
}
