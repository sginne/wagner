package com.norsula.wagner.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.norsula.wagner.model.fetchComicsWithCache
import com.norsula.wagner.model.Comic
import com.norsula.wagner.notification.NotificationHelper
import java.time.LocalTime

class ComicCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        println("ComicCheckWorker STARTED at ${System.currentTimeMillis()}")
        return try {
            val comics = fetchComicsWithCache(applicationContext)
            println("Комікс було перевірено останнього разу о ${LocalTime.now()}")
            val isNewComic = checkForNewComic(comics)
            if (isNewComic) {
                NotificationHelper.showNewComicNotification(applicationContext)
            }
            println("ComicCheckWorker COMPLETED successfully")
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            println("ComicCheckWorker FAILED: ${e.message}")
            Result.retry()
        }
    }

    private fun checkForNewComic(comics: List<Comic>): Boolean {
        // Implement your actual logic here
        return true // placeholder
    }
}