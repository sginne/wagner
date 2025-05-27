package com.viivi.wagner.ui.screens.devUi

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.imageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import kotlinx.coroutines.withContext




fun clearComicCache(context: Context) {
    val cacheDir = File(context.filesDir, "comics_cache")
    cacheDir.deleteRecursively()
    context.imageLoader.diskCache?.clear()
}

@Composable
fun DevPanel() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cacheSize by remember { mutableStateOf(0L) }

    fun calculateCacheSize(): Long {
        val cacheDir = File(context.filesDir, "comics_cache")
        return cacheDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }

    LaunchedEffect(Unit) {
        cacheSize = withContext(Dispatchers.IO) { calculateCacheSize() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .background(Color(0xAFAFAFAF), RoundedCornerShape(4.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Крута панель розробника", fontWeight = FontWeight.Bold)

        Text("Кеш: ${cacheSize / 1024} КБ")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    clearComicCache(context)
                    cacheSize = calculateCacheSize()
                }
            }) {
                Text("Почистити кеш")
            }
        }
    }
}
