package com.smashsonic.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

// Placeholder - downloads are handled by DownloadRepository with OkHttp for now.
// A foreground service can be added later for true background download support.
class DownloadService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
