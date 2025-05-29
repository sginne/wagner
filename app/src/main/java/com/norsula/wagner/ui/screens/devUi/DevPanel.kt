package com.norsula.wagner.ui.screens.devUi

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
import java.nio.file.Files
import java.nio.file.Path
import okio.Path.Companion.toOkioPath
import com.norsula.wagner.AppConfig


fun calculateCoilCacheItems(context: Context): Int {
    val diskCache = context.imageLoader.diskCache ?: return 0
    val dir = diskCache.directory ?: return 0
    val file = File(dir.toString())
    return file.listFiles()?.size ?: 0
}



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
    var cacheItems by remember { mutableStateOf(0) }
    var coilCacheItems by remember { mutableStateOf(0) }



    fun calculateCacheStats(): Pair<Long, Int> {
        val cacheDir = File(context.filesDir, "comics_cache")
        val files = cacheDir.walkTopDown().filter { it.isFile }.toList()
        val size = files.sumOf { it.length() }
        val count = files.size
        return size to count
    }

    LaunchedEffect(Unit) {
        val (size, count) = withContext(Dispatchers.IO) { calculateCacheStats() }
        val coilCount = withContext(Dispatchers.IO) { calculateCoilCacheItems(context) }
        coilCacheItems = coilCount
        cacheSize = size
        cacheItems = count
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

        Button(onClick = {
            AppConfig.debugMode.value = false
            AppConfig.comicClickCount.value = 0
        })
        {
            Text("Вимкнути debugMode")

        }
        Text("Крута панель розробника", fontWeight = FontWeight.Bold)
        Text("Кеш: ${cacheSize / 1024} КБ")
        Text("Елементів кешу: $cacheItems")
        Text("Кеш Coil: $coilCacheItems елементів")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    clearComicCache(context)
                    val (size, count) = calculateCacheStats()
                    val coilCount = calculateCoilCacheItems(context)
                    cacheSize = size
                    cacheItems = count
                    coilCacheItems = coilCount
                }
            }) {
                Text("Почистити кеш")
            }
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    val (size, count) = calculateCacheStats()
                    val coilCount = calculateCoilCacheItems(context)
                    cacheSize = size
                    cacheItems = count
                    coilCacheItems = coilCount
                }
            }) {
                Text("Оновити")
            }

        }
    }
}

