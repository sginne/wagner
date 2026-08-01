package com.norsula.wagner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.norsula.wagner.ui.MainScreen
import com.norsula.wagner.theme.WagnerTheme
import com.norsula.wagner.notification.NotificationHelper
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat





class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //println("App started")
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        enableEdgeToEdge()

        setContent {
            WagnerTheme {
                MainScreen(
                    initialComicId = intent.getStringExtra(
                        NotificationHelper.EXTRA_COMIC_ID
                    )
                )
            }
        }
    }
}
