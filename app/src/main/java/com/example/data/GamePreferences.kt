package com.example.data

import android.content.Context
import android.content.SharedPreferences

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("whacamole_kids_prefs", Context.MODE_PRIVATE)

    fun getHighScore(difficultyName: String): Int {
        return prefs.getInt("high_score_$difficultyName", 0)
    }

    fun saveHighScore(difficultyName: String, score: Int) {
        val current = getHighScore(difficultyName)
        if (score > current) {
            prefs.edit().putInt("high_score_$difficultyName", score).apply()
        }
    }

    var totalTickets: Int
        get() = prefs.getInt("total_tickets", 0)
        set(value) = prefs.edit().putInt("total_tickets", value).apply()

    var totalGamesPlayed: Int
        get() = prefs.getInt("total_games", 0)
        set(value) = prefs.edit().putInt("total_games", value).apply()

    var totalHitsAllTime: Int
        get() = prefs.getInt("total_hits", 0)
        set(value) = prefs.edit().putInt("total_hits", value).apply()

    var builderMolesHit: Int
        get() = prefs.getInt("builder_moles_hit", 0)
        set(value) = prefs.edit().putInt("builder_moles_hit", value).apply()

    var goldenMolesHit: Int
        get() = prefs.getInt("golden_moles_hit", 0)
        set(value) = prefs.edit().putInt("golden_moles_hit", value).apply()

    var maxComboAllTime: Int
        get() = prefs.getInt("max_combo", 0)
        set(value) = prefs.edit().putInt("max_combo", value).apply()

    var sfxEnabled: Boolean
        get() = prefs.getBoolean("sfx_enabled", true)
        set(value) = prefs.edit().putBoolean("sfx_enabled", value).apply()

    var musicEnabled: Boolean
        get() = prefs.getBoolean("music_enabled", true)
        set(value) = prefs.edit().putBoolean("music_enabled", value).apply()

    fun getUnlockedAchievements(): Set<String> {
        return prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()
    }

    fun unlockAchievement(id: String) {
        val current = getUnlockedAchievements().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet("unlocked_achievements", current).apply()
    }

    fun getClaimedPrizes(): Set<String> {
        return prefs.getStringSet("claimed_prizes", emptySet()) ?: emptySet()
    }

    fun claimPrize(id: String) {
        val current = getClaimedPrizes().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet("claimed_prizes", current).apply()
    }
}
