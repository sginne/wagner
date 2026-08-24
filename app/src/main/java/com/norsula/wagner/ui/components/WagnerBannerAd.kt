package com.norsula.wagner.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.norsula.wagner.BuildConfig

private const val TEST_BANNER_ID =
    "ca-app-pub-3940256099942544/6300978111"

private const val RELEASE_BANNER_ID =
    "ca-app-pub-9498264264251437/3718981593"

@Composable
internal fun WagnerBannerAd() {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val consentInformation = remember(context) {
        UserMessagingPlatform.getConsentInformation(context)
    }

    var canRequestAds by remember { mutableStateOf(false) }
    var mobileAdsReady by remember { mutableStateOf(false) }

    val adView = remember(context) {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = if (BuildConfig.DEBUG) {
                TEST_BANNER_ID
            } else {
                RELEASE_BANNER_ID
            }
        }
    }

    LaunchedEffect(activity) {
        val hostActivity = activity ?: return@LaunchedEffect
        val parameters = ConsentRequestParameters.Builder().build()

        consentInformation.requestConsentInfoUpdate(
            hostActivity,
            parameters,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    hostActivity
                ) {
                    canRequestAds = consentInformation.canRequestAds()
                }
            },
            {
                canRequestAds = consentInformation.canRequestAds()
            }
        )

        if (consentInformation.canRequestAds()) {
            canRequestAds = true
        }
    }

    LaunchedEffect(canRequestAds) {
        val hostActivity = activity ?: return@LaunchedEffect
        if (canRequestAds && !mobileAdsReady) {
            MobileAds.initialize(context) {
                hostActivity.runOnUiThread {
                    mobileAdsReady = true
                }
            }
        }
    }

    LaunchedEffect(mobileAdsReady, adView) {
        if (mobileAdsReady) {
            adView.loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    Box(
        modifier = Modifier
            .width(320.dp)
            .height(50.dp)
    ) {
        if (mobileAdsReady) {
            AndroidView(
                factory = { adView },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
