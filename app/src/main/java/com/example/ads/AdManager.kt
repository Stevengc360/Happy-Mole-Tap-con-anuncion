package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.BuildConfig
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
 * AdMob IDs. Debug builds always use Google's public TEST IDs (never click real ads yourself).
 * Release builds use the real IDs.
 */
object AdIds {
    private const val APP_ID_TEST = "ca-app-pub-3940256099942544~3347511713"
    private const val BANNER_TEST = "ca-app-pub-3940256099942544/9214589741"
    private const val INTERSTITIAL_TEST = "ca-app-pub-3940256099942544/1033173712"

    private const val APP_ID_REAL = "ca-app-pub-1781215997226138~6619174738"
    private const val BANNER_REAL = "ca-app-pub-1781215997226138/6427603043"
    private const val INTERSTITIAL_REAL = "ca-app-pub-1781215997226138/3761699220"

    val APP_ID: String = if (BuildConfig.DEBUG) APP_ID_TEST else APP_ID_REAL
    val BANNER: String = if (BuildConfig.DEBUG) BANNER_TEST else BANNER_REAL
    val INTERSTITIAL: String = if (BuildConfig.DEBUG) INTERSTITIAL_TEST else INTERSTITIAL_REAL
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

    /** [restricted] is true for minors (from the age screen): child-directed ad treatment. */
    fun initialize(context: Context, restricted: Boolean) {
        if (initStarted) return
        initStarted = true
        val appContext = context.applicationContext

        // The SDK must be initialized on a background thread.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestConfiguration = if (restricted) {
                    RequestConfiguration.Builder()
                        .setAgeRestrictedTreatment(AgeRestrictedTreatment.CHILD)
                        .build()
                } else {
                    RequestConfiguration.Builder().build()
                }

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
