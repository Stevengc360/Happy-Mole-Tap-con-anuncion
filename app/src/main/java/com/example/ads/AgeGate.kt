package com.example.ads

import android.content.Context
import java.util.Calendar

/**
 * Stores the birth year given on the neutral age screen and tells whether the user must be
 * treated as a minor (child-directed ad treatment, non-personalized ads).
 */
object AgeGate {

    private const val PREFS = "age_gate"
    private const val KEY_BIRTH_YEAR = "birth_year"

    // Only the birth year is asked, so the age can be off by one: be conservative.
    private const val ADULT_YEAR_DIFFERENCE = 19

    fun isAnswered(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(KEY_BIRTH_YEAR)

    fun saveBirthYear(context: Context, year: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_BIRTH_YEAR, year)
            .apply()
    }

    /** True when the user is (or may be) a minor, or has not answered yet. */
    fun isRestricted(context: Context): Boolean {
        val year = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_BIRTH_YEAR, -1)
        if (year < 0) return true
        return currentYear() - year < ADULT_YEAR_DIFFERENCE
    }

    fun isValidYear(year: Int): Boolean = year in 1900..currentYear()

    private fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
}
