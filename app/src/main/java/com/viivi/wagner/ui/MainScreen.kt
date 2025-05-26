package com.viivi.wagner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.viivi.wagner.R

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    Scaffold(
        bottomBar = {
            if (selectedTab != 0) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(painterResource(R.drawable.ic_home), contentDescription = "Головна") },
                        label = { Text("Головна") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(painterResource(R.drawable.ic_search), contentDescription = "Пошук") },
                        label = { Text("Пошук") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(painterResource(R.drawable.ic_info), contentDescription = "Інформація") },
                        label = { Text("Інформація") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomePage(selectedTab = { selectedTab = it })
                1 -> SearchPage()
                2 -> InfoPage()
            }
        }
    }
}
