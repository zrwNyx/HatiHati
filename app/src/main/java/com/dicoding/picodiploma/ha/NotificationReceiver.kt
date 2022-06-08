package com.dicoding.picodiploma.ha

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val dial = Intent(Intent.ACTION_DIAL)
        dial.setData(Uri.parse("tel:12345678910"))
    }
}