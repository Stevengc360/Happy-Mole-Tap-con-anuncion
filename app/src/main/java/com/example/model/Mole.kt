package com.example.model

enum class MoleState {
    HIDDEN,
    RISING,
    UP,
    HIT,
    RETREATING
}

enum class MoleType(
    val displayName: String,
    val points: Int,
    val tickets: Int,
    val requiresHits: Int = 1
) {
    NORMAL(
        displayName = "Topo Sonriente",
        points = 10,
        tickets = 1,
        requiresHits = 1
    ),
    CONSTRUCTOR(
        displayName = "Topo Obrero",
        points = 25,
        tickets = 2,
        requiresHits = 1 // Has helmet, gives big bonus!
    ),
    DORADO(
        displayName = "Rey Topo Dorado",
        points = 50,
        tickets = 5,
        requiresHits = 1 // Rare golden crown mole!
    ),
    TRAVIESO(
        displayName = "Topo Travieso",
        points = 15,
        tickets = 1,
        requiresHits = 1
    )
}

data class Mole(
    val id: Int,
    val state: MoleState = MoleState.HIDDEN,
    val type: MoleType = MoleType.NORMAL,
    val hitsTaken: Int = 0,
    val popUpTimestamp: Long = 0L,
    val hitTimestamp: Long = 0L
)
