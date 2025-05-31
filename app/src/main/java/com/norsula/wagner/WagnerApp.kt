package com.norsula.wagner

import android.app.Application
import androidx.work.*
import com.viivi.wagner.worker.ComicCheckWorker
import java.util.concurrent.TimeUnit

class WagnerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val workRequest = PeriodicWorkRequestBuilder<ComicCheckWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ComicCheckWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
