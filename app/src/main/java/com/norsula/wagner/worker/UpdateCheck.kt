package com.norsula.wagner.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.norsula.wagner.model.fetchComicsWithCache
import com.norsula.wagner.model.Comic
import com.norsula.wagner.notification.NotificationHelper
import java.time.LocalTime
import com.norsula.wagner.utils.LogUtil

class ComicCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        LogUtil.debug("ComicCheckWorker STARTED at ${System.currentTimeMillis()}")
        return try {
            val comics = fetchComicsWithCache(applicationContext)
            LogUtil.debug("Комікс було перевірено останнього разу о ${LocalTime.now()}")
            val isNewComic = checkForNewComic(comics)
            if (isNewComic) {
                if (isNewComic) {
                    val latestComic = comics.maxByOrNull { it.num ?: 0 }
                    latestComic?.let {
                        NotificationHelper.showNewComicNotification(applicationContext, comic = it)
                    }
                }

            }
            //println("ComicCheckWorker COMPLETED successfully")
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            println("ComicCheckWorker FAILED: ${e.message}")
            Result.retry()
        }
    }

    private fun checkForNewComic(comics: List<Comic>): Boolean {
        val latestComic = comics.maxByOrNull { it.num ?: 0 } ?: return false
        LogUtil.debug("Latest comic num in cache: ${latestComic?.num}")
        return true // placeholder
    }
}