package com.example.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gathers EU user consent with Google's User Messaging Platform (UMP) before any ad is requested.
 */
object ConsentManager {

    private const val TAG = "ConsentManager"

    private var consentInformation: ConsentInformation? = null
    private val adsStarted = AtomicBoolean(false)

    private val _privacyOptionsRequired = MutableStateFlow(false)

    /** True when the app must offer the user an entry point to change their privacy choices. */
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    /** Gathers consent and calls [onCanRequestAds] once (and only once) ads may be requested. */
    fun gatherConsent(activity: Activity, onCanRequestAds: () -> Unit) {
        val info = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation = info
        refreshPrivacyOptions(info)

        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(true) // the game is aimed at children
            .build()

        // Must be called on every app launch.
        info.requestConsentInfoUpdate(
            activity,
            params,
            {
                refreshPrivacyOptions(info)
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) Log.w(TAG, "Consent form error: ${formError.message}")
                    refreshPrivacyOptions(info)
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

    /** Shows the privacy options form so the user can review or change their choices. */
    fun showPrivacyOptionsForm(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) Log.w(TAG, "Privacy options error: ${formError.message}")
            consentInformation?.let { refreshPrivacyOptions(it) }
        }
    }

    private fun refreshPrivacyOptions(info: ConsentInformation) {
        _privacyOptionsRequired.value = info.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    private fun startAdsIfAllowed(info: ConsentInformation, onCanRequestAds: () -> Unit) {
        if (info.canRequestAds() && adsStarted.compareAndSet(false, true)) {
            onCanRequestAds()
        }
    }
}
