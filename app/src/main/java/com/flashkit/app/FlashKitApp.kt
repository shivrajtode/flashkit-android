package com.flashkit.app

import android.app.Application
import com.topjohnwu.superuser.Shell

class FlashKitApp : Application() {

    companion object {
        init {
            // Configure libsu: auto-manage shell lifecycle
            Shell.enableVerboseLogging = BuildConfig.DEBUG
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                    .setTimeout(30)
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Pre-warm root shell in background
        Shell.getShell { }
    }
}
