package com.norsula.wagner.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.norsula.wagner.MainActivity
import com.norsula.wagner.R
import com.norsula.wagner.model.Comic
import com.norsula.wagner.utils.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationHelper {
    const val EXTRA_COMIC_ID = "comic_id"

    private const val CHANNEL_ID = "new_comic_channel"
    private const val NOTIFICATION_ID = 414232

    fun showNewComicNotification(context: Context, comic: Comic) {
        CoroutineScope(Dispatchers.IO).launch {
            showNotification(context.applicationContext, comic)
        }
    }

    private suspend fun showNotification(context: Context, comic: Comic) {
        createChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_COMIC_ID, comic.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            comic.id?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bitmap = try {
            val request = ImageRequest.Builder(context)
                .data(comic.image)
                .allowHardware(false)
                .build()
            val result = ImageLoader(context).execute(request)
            (result as? SuccessResult)?.drawable?.toBitmap()
        } catch (e: Exception) {
            LogUtil.error("Failed to load notification image", e)
            null
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(comic.title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (bitmap != null) {
            builder
                .setLargeIcon(bitmap)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null as Bitmap?)
                )
        } else {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(comic.title)
            )
        }

        try {
            val manager = NotificationManagerCompat.from(context)
            if (manager.areNotificationsEnabled()) {
                manager.notify(NOTIFICATION_ID, builder.build())
                LogUtil.debug("Notification displayed successfully")
            }
        } catch (e: SecurityException) {
            LogUtil.error("Notification permission missing", e)
        }
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_new_comics),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}
