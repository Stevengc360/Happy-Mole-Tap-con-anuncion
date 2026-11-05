package com.example.model

enum class Difficulty(
    val title: String,
    val subtitle: String,
    val icon: String,
    val moleVisibleDurationMs: Long,
    val spawnIntervalMs: Long,
    val maxActiveMoles: Int,
    val colorHex: Long
) {
    FACIL(
        title = "Fácil",
        subtitle = "Ritmo suave para los más peques",
        icon = "🐢",
        moleVisibleDurationMs = 1700L,
        spawnIntervalMs = 1200L,
        maxActiveMoles = 1,
        colorHex = 0xFF4CAF50
    ),
    MEDIO(
        title = "Normal",
        subtitle = "Velocidad estándar divertida",
        icon = "🐰",
        moleVisibleDurationMs = 1100L,
        spawnIntervalMs = 850L,
        maxActiveMoles = 2,
        colorHex = 0xFFFF9800
    ),
    DIFICIL(
        title = "Rápido",
        subtitle = "¡Reflejos ninja relámpago!",
        icon = "⚡",
        moleVisibleDurationMs = 700L,
        spawnIntervalMs = 550L,
        maxActiveMoles = 3,
        colorHex = 0xFFE91E63
    )
}
