package com.viivi.wagner.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.norsula.wagner.model.fetchComicsWithCache // your fetching util
import com.norsula.wagner.model.Comic
import com.norsula.wagner.notification.NotificationHelper // to create notification, create if needed

class ComicCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val comics = fetchComicsWithCache(applicationContext)
            // TODO: compare with saved comic id or date to detect new comic
            val isNewComic = checkForNewComic(comics)
            if (isNewComic) {
                NotificationHelper.showNewComicNotification(applicationContext)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun checkForNewComic(comics: List<Comic>): Boolean {
        // implement logic to detect new comic
        return true // placeholder
    }
}
