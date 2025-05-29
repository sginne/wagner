package com.norsula.wagner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.norsula.wagner.ui.MainScreen
import com.norsula.wagner.theme.WagnerTheme

import com.norsula.wagner.ui.screens.HomePage
import com.norsula.wagner.ui.screens.SearchPage
import com.norsula.wagner.ui.screens.InfoPage


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            WagnerTheme {
                MainScreen()
            }
        }
    }
}
