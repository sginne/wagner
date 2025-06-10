package com.norsula.wagner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.norsula.wagner.model.fetchWagnerInfoWithCache
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import android.text.Html
import android.widget.TextView
import androidx.compose.ui.viewinterop.AndroidView




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoPage(modifier: Modifier = Modifier) {
    val (wagnerInfo, setWagnerInfo) = remember { mutableStateOf<String?>(null) }
    val (isLoading, setIsLoading) = remember { mutableStateOf(true) }
    val (error, setError) = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        try {
            val info = fetchWagnerInfoWithCache(context)
            setWagnerInfo(info?.content ?: "No information available")
        } catch (e: Exception) {
            setError("Failed to load information: ${e.localizedMessage}")
        } finally {
            setIsLoading(false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Information") },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back navigation */ }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )

                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.padding(16.dp))
                error != null -> Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                wagnerInfo != null -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    HtmlText(htmlContent = wagnerInfo ?: "No information available")
                    //Text(
                    //    text = wagnerInfo,
                    //   modifier = Modifier.padding(16.dp)
                    //)
                    // Add extra space at the bottom
                    Spacer(modifier = Modifier.height(32.dp))
                }
                else -> Text(
                    "No data available",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
@Composable
fun HtmlText(htmlContent: String) {
    AndroidView(
        factory = { context ->
            TextView(context).apply {
                text = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT)
            }
        },
        update = { it.text = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT) }
    )
}
