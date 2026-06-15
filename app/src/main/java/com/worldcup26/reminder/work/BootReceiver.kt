package com.worldcup26.reminder.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.worldcup26.reminder.WorldCupApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Exact alarms do not survive a reboot or app update, so we re-arm every pending
 * reminder from the persisted selection list when the device comes back up.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val pending = goAsync()
        val repo = (context.applicationContext as WorldCupApp).container.matchRepository
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repo.rescheduleFollowed()
            } finally {
                pending.finish()
            }
        }
    }
}
