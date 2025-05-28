package com.viivi.wagner.ui


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import com.viivi.wagner.model.Comic

import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Box

import androidx.compose.ui.Modifier

import androidx.compose.foundation.layout.padding
import com.viivi.wagner.ui.screens.HomePage
import com.viivi.wagner.ui.screens.SearchPage
import com.viivi.wagner.ui.screens.InfoPage










@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var comics by remember { mutableStateOf<List<Comic>?>(null) }

    var selectedComicId by remember { mutableStateOf<String?>(null) }

    val navigateToHomePageWithComicId = remember {
        { id: String ->
            selectedComicId = id
            selectedTab = 0
        }
    }

    // Логіку завантаження коміксів можна винести сюди, або в HomePage і підняти стан через callback

    Scaffold(
        bottomBar = { /* твій код */ }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomePage(
                    selectedTab = { selectedTab = it },
                    onComicsLoaded = { comics = it }, // додай callback в HomePage
                            initialComicId = "2025/05/24/vivi-ta-vagner-priyemni-ta-nepriyemni-syurprizi/"

                )
                1 -> SearchPage(
                    comics = comics,
                    onSelect = { comic -> navigateToHomePageWithComicId(comic.id ?: "") }
                )
                2 -> InfoPage()
            }
        }
    }
}
