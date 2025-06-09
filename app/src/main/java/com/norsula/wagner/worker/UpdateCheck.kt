package com.norsula.wagner.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.norsula.wagner.model.fetchComicsWithCache
import com.norsula.wagner.model.Comic
import com.norsula.wagner.notification.NotificationHelper
import java.time.LocalTime
import com.norsula.wagner.utils.LogUtil
import com.norsula.wagner.AppConfig

class ComicCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        LogUtil.debug("ComicCheckWorker STARTED at ${System.currentTimeMillis()}")
        return try {
            AppConfig.load(applicationContext)

            val prefs = applicationContext.getSharedPreferences("app_config", Context.MODE_PRIVATE)
            LogUtil.debug("Loaded lastCheckedNum from prefs: ${prefs.getInt("lastCheckedNum", -1)}")

            val comics = fetchComicsWithCache(applicationContext)
            val isNewComic = checkForNewComic(comics)

            if (isNewComic) {
                val latestComic = comics.maxByOrNull { it.num ?: 0 }
                latestComic?.let { comic ->
                    NotificationHelper.showNewComicNotification(applicationContext, comic)
                    comic.num?.let { num ->
                        AppConfig.lastCheckedNum.value = num
                        AppConfig.save(applicationContext)
                        LogUtil.debug("Updated lastCheckedNum to $num and saved")
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            println("ComicCheckWorker FAILED: ${e.message}")
            Result.retry()
        }
    }

    private fun checkForNewComic(comics: List<Comic>): Boolean {
        val latestComic = comics.maxByOrNull { it.num ?: 0 } ?: return false
        val lastSaved = AppConfig.lastCheckedNum.value
        val latestNum = latestComic.num ?: 0

        LogUtil.debug("Comparing: latest = $latestNum, saved = $lastSaved")
        return latestNum > lastSaved
    }
}