package com.norsula.wagner.ui


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import com.norsula.wagner.model.Comic

import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Box

import androidx.compose.ui.Modifier

import androidx.compose.foundation.layout.padding
import com.norsula.wagner.ui.screens.HomePage
import com.norsula.wagner.ui.screens.SearchPage
import com.norsula.wagner.ui.screens.InfoPage
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack













@OptIn(ExperimentalMaterial3Api::class)
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
                    onComicsLoaded = { comics = it },
                    initialComicId = selectedComicId

                    //initialComicId = "2025/05/24/vivi-ta-vagner-priyemni-ta-nepriyemni-syurprizi/"

                )
                1 -> Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Search") },
                            navigationIcon = {
                                IconButton(onClick = { selectedTab = 0 }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { padding ->
                    SearchPage(
                        comics = comics,
                        onSelect = { comic -> navigateToHomePageWithComicId(comic.id ?: "") },
                        modifier = Modifier.padding(padding)
                    )
                }

                2 -> Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Info") },
                            navigationIcon = {
                                IconButton(onClick = { selectedTab = 0 }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { padding ->
                    InfoPage(modifier = Modifier.padding(padding))
                }

            }
        }
    }
}
