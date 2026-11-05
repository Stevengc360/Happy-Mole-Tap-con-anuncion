package com.example.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Gathers EU user consent with Google's User Messaging Platform (UMP) before any ad is requested.
 */
object ConsentManager {

    private const val TAG = "ConsentManager"

    private var consentInformation: ConsentInformation? = null
    private val adsStarted = AtomicBoolean(false)

    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation?.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /** Gathers consent and calls [onCanRequestAds] once (and only once) ads may be requested. */
    fun gatherConsent(activity: Activity, onCanRequestAds: () -> Unit) {
        val info = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation = info

        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(true) // the game is aimed at children
            .build()

        // Must be called on every app launch.
        info.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) Log.w(TAG, "Consent form error: ${formError.message}")
                    startAdsIfAllowed(info, onCanRequestAds)
                }
            },
            { requestError ->
                Log.w(TAG, "Consent info update error: ${requestError.message}")
                startAdsIfAllowed(info, onCanRequestAds)
            }
        )

        // Consent may have been obtained in a previous session.
        startAdsIfAllowed(info, onCanRequestAds)
    }

    fun showPrivacyOptionsForm(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) Log.w(TAG, "Privacy options error: ${formError.message}")
        }
    }

    private fun startAdsIfAllowed(info: ConsentInformation, onCanRequestAds: () -> Unit) {
        if (info.canRequestAds() && adsStarted.compareAndSet(false, true)) {
            onCanRequestAds()
        }
    }
}
