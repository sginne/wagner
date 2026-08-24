package com.norsula.wagner.ui.screens

import android.text.Html
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.norsula.wagner.model.fetchWagnerInfoWithCache

@Composable
fun InfoPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    var wagnerInfo by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        try {
            val info = fetchWagnerInfoWithCache(context)
            wagnerInfo = info?.content ?: "Інформація відсутня"
        } catch (exception: Exception) {
            error = "Не вдалося завантажити інформацію: " +
                exception.localizedMessage
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }

            error != null -> {
                Text(
                    text = error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            wagnerInfo != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    HtmlText(
                        htmlContent = wagnerInfo.orEmpty(),
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            else -> {
                Text(
                    text = "Немає даних",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun HtmlText(
    htmlContent: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                text = Html.fromHtml(
                    htmlContent,
                    Html.FROM_HTML_MODE_COMPACT
                )
            }
        },
        update = {
            it.text = Html.fromHtml(
                htmlContent,
                Html.FROM_HTML_MODE_COMPACT
            )
        }
    )
}
