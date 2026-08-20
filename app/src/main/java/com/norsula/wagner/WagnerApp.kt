package com.norsula.wagner

import android.app.Application
import com.google.android.gms.ads.MobileAds
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.norsula.wagner.worker.ComicCheckWorker
import java.util.concurrent.TimeUnit

class WagnerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
        setupComicCheckWorker()
    }

    private fun setupComicCheckWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = PeriodicWorkRequestBuilder<ComicCheckWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(constraints).build()

        WorkManager.getInstance(this).apply {
            enqueueUniquePeriodicWork(
                "ComicCheckWork",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}