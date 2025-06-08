package com.norsula.wagner.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.norsula.wagner.R
import com.norsula.wagner.utils.LogUtil
import com.norsula.wagner.model.Comic


object NotificationHelper {
    private const val CHANNEL_ID = "new_comic_channel"
    private const val CHANNEL_NAME = "New Comic Alerts"
    private const val NOTIFICATION_ID = 414232 // Fixed ID for all comic notifications


    fun showNewComicNotification(context: Context, comic:Comic) {
        LogUtil.debug("Preparing new comic notification")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LogUtil.debug("Creating notification channel")
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                LogUtil.debug("Channel configured with importance: DEFAULT")
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            LogUtil.debug("Notification channel created")
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // put a real icon in res/drawable
            .setContentTitle("New Comic Available!")
            .setContentText("New comic: ${comic.title}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
                LogUtil.debug("Notification displayed successfully (ID: $NOTIFICATION_ID)")
            }
        } catch (e: Exception) {
            LogUtil.error("Failed to show notification", e)
        }
    }
}
