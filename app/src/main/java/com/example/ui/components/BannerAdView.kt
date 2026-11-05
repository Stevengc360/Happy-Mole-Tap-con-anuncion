package com.example.ui.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ads.AdIds
import com.example.ads.AdManager
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

private const val TAG = "BannerAdView"

/**
 * Anchored adaptive banner. Loads once the Ads SDK is ready.
 */
@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) return

    val context = LocalContext.current
    val adView = remember { AdView(context) }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val sdkReady by AdManager.ready.collectAsState()

    LaunchedEffect(sdkReady, screenWidthDp) {
        if (!sdkReady) return@LaunchedEffect

        val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
        val adRequest = BannerAdRequest.Builder(AdIds.BANNER, adSize).build()

        adView.loadAd(
            adRequest,
            object : AdLoadCallback<BannerAd> {
                override fun onAdLoaded(ad: BannerAd) {
                    Log.d(TAG, "Banner ad loaded.")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.w(TAG, "Banner ad failed to load: $adError")
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose { adView.destroy() }
    }

    AndroidView(
        modifier = modifier.fillMaxWidth().wrapContentHeight(),
        factory = { adView }
    )
}
