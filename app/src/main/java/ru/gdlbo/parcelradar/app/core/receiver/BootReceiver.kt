package ru.gdlbo.parcelradar.app.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.gdlbo.parcelradar.app.core.service.BackgroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, BackgroundService::class.java)
            context.startService(serviceIntent)
        }
    }
}