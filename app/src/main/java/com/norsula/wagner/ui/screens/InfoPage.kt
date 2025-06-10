package com.norsula.wagner.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewState
import com.google.accompanist.web.rememberWebViewState
import android.webkit.WebSettings


@Composable
fun InfoPage(modifier: Modifier = Modifier) {
    //val url = "https://norsula.com/wagner-app/"
    val url = "https://google.com/"
    val state = rememberWebViewState(url)

    Box(modifier = modifier.fillMaxSize()) {
        WebView(state = state,
            modifier = Modifier.fillMaxSize(),
            onCreated = { webView ->
                webView.settings.javaScriptEnabled = true
                webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        })
    }
}
