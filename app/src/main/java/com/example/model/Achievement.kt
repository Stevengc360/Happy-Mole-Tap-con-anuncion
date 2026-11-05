package com.example.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val isUnlocked: Boolean = false,
    val rewardTickets: Int = 5
) {
    val progressFraction: Float
        get() = (currentProgress.toFloat() / targetProgress.toFloat()).coerceIn(0f, 1f)
}
