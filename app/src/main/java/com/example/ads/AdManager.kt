package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AgeRestrictedTreatment
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdPreloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AdMob IDs. These are Google's public TEST IDs.
 * Replace them with your own IDs before publishing the app.
 */
object AdIds {
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val BANNER = "ca-app-pub-3940256099942544/9214589741"
    const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
}

/**
 * Initializes the GMA Next-Gen SDK, preloads interstitials and shows them on request.
 */
object AdManager {

    private const val TAG = "AdManager"

    /** An interstitial is shown every N finished games. */
    const val GAMES_PER_INTERSTITIAL = 2

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    private var initStarted = false

    fun initialize(context: Context) {
        if (initStarted) return
        initStarted = true
        val appContext = context.applicationContext

        // The SDK must be initialized on a background thread.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // The game is aimed at children: request child age treatment.
                val requestConfiguration = RequestConfiguration.Builder()
                    .setAgeRestrictedTreatment(AgeRestrictedTreatment.CHILD)
                    .build()

                MobileAds.initialize(
                    appContext,
                    InitializationConfig.Builder(AdIds.APP_ID)
                        .setRequestConfiguration(requestConfiguration)
                        .build()
                ) {
                    Log.d(TAG, "Ad adapters initialized.")
                }

                val adRequest = AdRequest.Builder(AdIds.INTERSTITIAL).build()
                InterstitialAdPreloader.start(AdIds.INTERSTITIAL, PreloadConfiguration(adRequest))

                _ready.value = true
            } catch (e: Exception) {
                Log.w(TAG, "Ad SDK initialization failed: $e")
            }
        }
    }

    /**
     * Shows a preloaded interstitial if one is available, then calls [onDone].
     * If no ad is available (or it fails to show), [onDone] is called right away so the game never waits.
     */
    fun showInterstitial(activity: Activity, onDone: () -> Unit) {
        if (!_ready.value) {
            onDone()
            return
        }

        val ad = try {
            InterstitialAdPreloader.pollAd(AdIds.INTERSTITIAL)
        } catch (e: Exception) {
            Log.w(TAG, "Could not poll interstitial: $e")
            null
        }

        if (ad == null) {
            Log.d(TAG, "Interstitial ad is not available.")
            onDone()
            return
        }

        ad.adEventCallback = object : InterstitialAdEventCallback {
            override fun onAdDismissedFullScreenContent() {
                activity.runOnUiThread { onDone() }
            }

            override fun onAdFailedToShowFullScreenContent(
                fullScreenContentError: FullScreenContentError
            ) {
                Log.w(TAG, "Interstitial failed to show: ${fullScreenContentError.message}")
                activity.runOnUiThread { onDone() }
            }
        }

        ad.show(activity)
    }
}
