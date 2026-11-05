package com.example.model

data class ArcadePrize(
    val id: String,
    val name: String,
    val description: String,
    val costTickets: Int,
    val iconEmoji: String,
    val isClaimed: Boolean = false
)
