package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.ArcadeAudioManager
import com.example.data.GamePreferences
import com.example.model.Achievement
import com.example.model.ArcadePrize
import com.example.model.Difficulty
import com.example.model.Mole
import com.example.model.MoleState
import com.example.model.MoleType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppScreen {
    MENU,
    GAME,
    ACHIEVEMENTS,
    PRIZES
}

data class FloatingHitEffect(
    val id: Long,
    val holeIndex: Int?,
    val text: String,
    val isCritical: Boolean = false,
    val isMiss: Boolean = false
)

data class GameUiState(
    val currentScreen: AppScreen = AppScreen.MENU,
    val difficulty: Difficulty = Difficulty.FACIL,
    val moles: List<Mole> = List(6) { Mole(id = it) },
    val score: Int = 0,
    val hits: Int = 0,
    val misses: Int = 0,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val ticketsThisGame: Int = 0,
    val totalTickets: Int = 0,
    val timeLeftSeconds: Int = 30,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val highScore: Int = 0,
    val sfxEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val activeMalletHole: Int? = null,
    val floatingEffects: List<FloatingHitEffect> = emptyList(),
    val newlyUnlockedAchievement: Achievement? = null,
    val achievements: List<Achievement> = emptyList(),
    val prizes: List<ArcadePrize> = emptyList()
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = GamePreferences(application)
    val audioManager = ArcadeAudioManager(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameTimerJob: Job? = null
    private var moleSpawnerJob: Job? = null
    private val moleDismissJobs = mutableMapOf<Int, Job>()

    private var nextEffectId = 0L

    private fun createEffectId(): Long = nextEffectId++

    init {
        audioManager.isSfxEnabled = prefs.sfxEnabled
        audioManager.isMusicEnabled = prefs.musicEnabled

        _uiState.update { state ->
            state.copy(
                sfxEnabled = prefs.sfxEnabled,
                musicEnabled = prefs.musicEnabled,
                totalTickets = prefs.totalTickets,
                highScore = prefs.getHighScore(state.difficulty.name),
                achievements = buildAchievementsList(),
                prizes = buildPrizesList()
            )
        }
    }

    fun onAppResumed() {
        val state = _uiState.value
        if (state.musicEnabled && !state.isPaused) {
            audioManager.startBackgroundMusic()
        }
    }

    fun onAppPaused() {
        audioManager.stopMusic()
    }

    private fun buildAchievementsList(): List<Achievement> {
        val unlockedIds = prefs.getUnlockedAchievements()
        val allTimeHits = prefs.totalHitsAllTime
        val maxCombo = prefs.maxComboAllTime
        val builderHits = prefs.builderMolesHit
        val goldenHits = prefs.goldenMolesHit
        val totalTickets = prefs.totalTickets
        val gamesPlayed = prefs.totalGamesPlayed

        return listOf(
            Achievement(
                id = "first_hit",
                title = "¡Primer Martillazo!",
                description = "Golpea a tu primer topo en el tablero",
                icon = "🔨",
                currentProgress = allTimeHits.coerceAtMost(1),
                targetProgress = 1,
                isUnlocked = unlockedIds.contains("first_hit") || allTimeHits >= 1,
                rewardTickets = 3
            ),
            Achievement(
                id = "combo_5",
                title = "¡Reflejos de Conejito!",
                description = "Consigue una racha de 5 aciertos seguidos",
                icon = "🐰",
                currentProgress = maxCombo.coerceAtMost(5),
                targetProgress = 5,
                isUnlocked = unlockedIds.contains("combo_5") || maxCombo >= 5,
                rewardTickets = 5
            ),
            Achievement(
                id = "builder_hunter",
                title = "¡Cazador de Cascos!",
                description = "Golpea a 5 topos constructores con casco",
                icon = "👷",
                currentProgress = builderHits.coerceAtMost(5),
                targetProgress = 5,
                isUnlocked = unlockedIds.contains("builder_hunter") || builderHits >= 5,
                rewardTickets = 8
            ),
            Achievement(
                id = "golden_king",
                title = "¡El Rey Topo!",
                description = "Encuentra y golpea a 2 Reyes Topo Dorados",
                icon = "👑",
                currentProgress = goldenHits.coerceAtMost(2),
                targetProgress = 2,
                isUnlocked = unlockedIds.contains("golden_king") || goldenHits >= 2,
                rewardTickets = 15
            ),
            Achievement(
                id = "ticket_storm",
                title = "¡Lluvia de Tickets!",
                description = "Acumula 30 tickets de las máquinas de feria",
                icon = "🎟️",
                currentProgress = totalTickets.coerceAtMost(30),
                targetProgress = 30,
                isUnlocked = unlockedIds.contains("ticket_storm") || totalTickets >= 30,
                rewardTickets = 10
            ),
            Achievement(
                id = "ninja_hits",
                title = "¡Maestro de la Puntería!",
                description = "Alcanza 50 aciertos en total",
                icon = "🎯",
                currentProgress = allTimeHits.coerceAtMost(50),
                targetProgress = 50,
                isUnlocked = unlockedIds.contains("ninja_hits") || allTimeHits >= 50,
                rewardTickets = 12
            ),
            Achievement(
                id = "arcade_lover",
                title = "¡Estrella Recreativa!",
                description = "Juega 5 partidas en la máquina recreativa",
                icon = "⭐",
                currentProgress = gamesPlayed.coerceAtMost(5),
                targetProgress = 5,
                isUnlocked = unlockedIds.contains("arcade_lover") || gamesPlayed >= 5,
                rewardTickets = 10
            )
        )
    }

    private fun buildPrizesList(): List<ArcadePrize> {
        val claimed = prefs.getClaimedPrizes()
        return listOf(
            ArcadePrize(
                id = "prize_bear",
                name = "Osito de Peluche Suave",
                description = "Un tierno peluche ganado en la feria",
                costTickets = 15,
                iconEmoji = "🧸",
                isClaimed = claimed.contains("prize_bear")
            ),
            ArcadePrize(
                id = "prize_hammer",
                name = "Martillo Gigante Hinchable",
                description = "¡El clásico martillo de rayas de colores!",
                costTickets = 25,
                iconEmoji = "🔨",
                isClaimed = claimed.contains("prize_hammer")
            ),
            ArcadePrize(
                id = "prize_crown",
                name = "Corona Brillante de Rey",
                description = "¡Para el campeón o campeona indiscutible!",
                costTickets = 40,
                iconEmoji = "👑",
                isClaimed = claimed.contains("prize_crown")
            ),
            ArcadePrize(
                id = "prize_balloon",
                name = "Globo Mágico Volador",
                description = "Colorido globo de fiesta para llevar",
                costTickets = 10,
                iconEmoji = "🎈",
                isClaimed = claimed.contains("prize_balloon")
            ),
            ArcadePrize(
                id = "prize_trophy",
                name = "Gran Trofeo de Oro Arcade",
                description = "El premio más deseado de toda la sala de juegos",
                costTickets = 60,
                iconEmoji = "🏆",
                isClaimed = claimed.contains("prize_trophy")
            )
        )
    }

    fun selectDifficulty(diff: Difficulty) {
        _uiState.update {
            it.copy(
                difficulty = diff,
                highScore = prefs.getHighScore(diff.name)
            )
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun toggleSfx() {
        val newState = !prefs.sfxEnabled
        prefs.sfxEnabled = newState
        audioManager.isSfxEnabled = newState
        _uiState.update { it.copy(sfxEnabled = newState) }
    }

    fun toggleMusic() {
        val newState = !prefs.musicEnabled
        prefs.musicEnabled = newState
        audioManager.isMusicEnabled = newState
        _uiState.update { it.copy(musicEnabled = newState) }
        if (newState) {
            audioManager.startBackgroundMusic()
        } else {
            audioManager.stopMusic()
        }
    }

    fun startGame() {
        stopAllGameJobs()

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.GAME,
                moles = List(6) { index -> Mole(id = index) },
                score = 0,
                hits = 0,
                misses = 0,
                combo = 0,
                ticketsThisGame = 0,
                timeLeftSeconds = 30,
                isPlaying = true,
                isPaused = false,
                isGameOver = false,
                activeMalletHole = null,
                floatingEffects = emptyList(),
                newlyUnlockedAchievement = null,
                highScore = prefs.getHighScore(it.difficulty.name)
            )
        }

        prefs.totalGamesPlayed = prefs.totalGamesPlayed + 1
        checkAchievements()

        startGameTimer()
        startMoleSpawner()
    }

    private fun startGameTimer() {
        gameTimerJob?.cancel()
        gameTimerJob = viewModelScope.launch {
            while (_uiState.value.timeLeftSeconds > 0 && _uiState.value.isPlaying) {
                delay(1000L)
                if (!_uiState.value.isPaused) {
                    _uiState.update { it.copy(timeLeftSeconds = it.timeLeftSeconds - 1) }
                }
            }
            if (_uiState.value.isPlaying) {
                finishGame()
            }
        }
    }

    private fun startMoleSpawner() {
        moleSpawnerJob?.cancel()
        moleSpawnerJob = viewModelScope.launch {
            val diff = _uiState.value.difficulty
            while (_uiState.value.isPlaying && !_uiState.value.isGameOver) {
                if (!_uiState.value.isPaused) {
                    spawnRandomMole()
                }
                delay(diff.spawnIntervalMs)
            }
        }
    }

    private fun spawnRandomMole() {
        val state = _uiState.value
        val activeCount = state.moles.count { it.state == MoleState.UP || it.state == MoleState.RISING }
        if (activeCount >= state.difficulty.maxActiveMoles) return

        val hiddenMoles = state.moles.filter { it.state == MoleState.HIDDEN }
        if (hiddenMoles.isEmpty()) return

        val targetHole = hiddenMoles.random()
        val randomRoll = Random.nextInt(100)
        val moleType = when {
            randomRoll < 20 -> MoleType.CONSTRUCTOR // 20% construction mole with helmet
            randomRoll < 28 -> MoleType.DORADO // 8% golden king mole
            randomRoll < 48 -> MoleType.TRAVIESO // 20% cheeky mole
            else -> MoleType.NORMAL // 52% normal mole
        }

        audioManager.playPopUpSound()

        val updatedMole = targetHole.copy(
            state = MoleState.UP,
            type = moleType,
            hitsTaken = 0,
            popUpTimestamp = System.currentTimeMillis()
        )

        _uiState.update { current ->
            current.copy(
                moles = current.moles.map { if (it.id == targetHole.id) updatedMole else it }
            )
        }

        // Schedule retreat when time is up
        moleDismissJobs[targetHole.id]?.cancel()
        moleDismissJobs[targetHole.id] = viewModelScope.launch {
            delay(_uiState.value.difficulty.moleVisibleDurationMs)
            hideMole(targetHole.id)
        }
    }

    private fun hideMole(holeIndex: Int) {
        val mole = _uiState.value.moles.getOrNull(holeIndex) ?: return
        if (mole.state == MoleState.UP) {
            // Mole escaped without being hit
            _uiState.update { current ->
                current.copy(
                    combo = 0, // reset combo
                    moles = current.moles.map {
                        if (it.id == holeIndex) it.copy(state = MoleState.HIDDEN) else it
                    }
                )
            }
        } else if (mole.state == MoleState.HIT) {
            _uiState.update { current ->
                current.copy(
                    moles = current.moles.map {
                        if (it.id == holeIndex) it.copy(state = MoleState.HIDDEN) else it
                    }
                )
            }
        }
    }

    fun handleMiss(holeIndex: Int? = null) {
        val state = _uiState.value
        if (!state.isPlaying || state.isPaused || state.isGameOver) return

        // Play comical 'miss' sound effect on empty space tap
        audioManager.playMissSound()

        val newMisses = state.misses + 1
        val missEffect = FloatingHitEffect(
            id = createEffectId(),
            holeIndex = holeIndex,
            text = "¡Uy! 💨",
            isCritical = false,
            isMiss = true
        )

        // Show brief mallet swing if tapped on a hole
        if (holeIndex != null) {
            _uiState.update { it.copy(activeMalletHole = holeIndex) }
            viewModelScope.launch {
                delay(200L)
                _uiState.update { if (it.activeMalletHole == holeIndex) it.copy(activeMalletHole = null) else it }
            }
        }

        _uiState.update { current ->
            current.copy(
                misses = newMisses,
                combo = 0, // Reset combo on miss
                floatingEffects = current.floatingEffects + missEffect
            )
        }

        // Clean up floating effect after 600ms
        viewModelScope.launch {
            delay(600L)
            _uiState.update { current ->
                current.copy(floatingEffects = current.floatingEffects.filter { it.id != missEffect.id })
            }
        }
    }

    fun tapEmptySpace() {
        handleMiss(null)
    }

    fun whackMole(holeIndex: Int) {
        val state = _uiState.value
        if (!state.isPlaying || state.isPaused || state.isGameOver) return

        val mole = state.moles.getOrNull(holeIndex) ?: return
        if (mole.state != MoleState.UP) {
            // Tap on empty hole or already hit -> Miss sound!
            handleMiss(holeIndex)
            return
        }

        // Play mallet strike
        _uiState.update { it.copy(activeMalletHole = holeIndex) }
        viewModelScope.launch {
            delay(280L)
            _uiState.update { if (it.activeMalletHole == holeIndex) it.copy(activeMalletHole = null) else it }
        }

        val type = mole.type
        val isGolden = type == MoleType.DORADO
        val isConstructor = type == MoleType.CONSTRUCTOR

        val newCombo = state.combo + 1

        // Play upbeat sound effect when mole is successfully hit!
        if (isGolden) {
            audioManager.playStarChime()
        } else {
            audioManager.playUpbeatHitSound(combo = newCombo, isSpecial = isConstructor)
        }

        val comboBonus = if (newCombo >= 3) (newCombo * 2) else 0
        val pointsEarned = type.points + comboBonus
        val ticketsEarned = type.tickets

        audioManager.playTicketSound()

        val newHits = state.hits + 1
        val newScore = state.score + pointsEarned
        val newTicketsThisGame = state.ticketsThisGame + ticketsEarned
        val newMaxCombo = maxOf(state.maxCombo, newCombo)

        // Update persistent stats
        prefs.totalHitsAllTime = prefs.totalHitsAllTime + 1
        if (isConstructor) prefs.builderMolesHit = prefs.builderMolesHit + 1
        if (isGolden) prefs.goldenMolesHit = prefs.goldenMolesHit + 1
        prefs.maxComboAllTime = maxOf(prefs.maxComboAllTime, newMaxCombo)

        val hitText = buildString {
            append("+$pointsEarned")
            if (newCombo >= 3) append(" (x$newCombo!)")
        }

        val effect = FloatingHitEffect(
            id = createEffectId(),
            holeIndex = holeIndex,
            text = hitText,
            isCritical = isGolden || newCombo >= 5,
            isMiss = false
        )

        val updatedMole = mole.copy(
            state = MoleState.HIT,
            hitTimestamp = System.currentTimeMillis(),
            hitsTaken = mole.hitsTaken + 1
        )

        _uiState.update { current ->
            current.copy(
                score = newScore,
                hits = newHits,
                combo = newCombo,
                maxCombo = newMaxCombo,
                ticketsThisGame = newTicketsThisGame,
                moles = current.moles.map { if (it.id == holeIndex) updatedMole else it },
                floatingEffects = current.floatingEffects + effect
            )
        }

        // Clean up floating effect after 750ms
        viewModelScope.launch {
            delay(750L)
            _uiState.update { current ->
                current.copy(floatingEffects = current.floatingEffects.filter { it.id != effect.id })
            }
        }

        // Keep hit mole visible with dizzy face for 500ms before slipping down
        moleDismissJobs[holeIndex]?.cancel()
        moleDismissJobs[holeIndex] = viewModelScope.launch {
            delay(520L)
            hideMole(holeIndex)
        }

        checkAchievements()
    }

    private fun checkAchievements() {
        val currentAchievements = buildAchievementsList()
        val unlockedIds = prefs.getUnlockedAchievements().toMutableSet()

        for (ach in currentAchievements) {
            if (!unlockedIds.contains(ach.id) && ach.currentProgress >= ach.targetProgress) {
                // Newly unlocked!
                prefs.unlockAchievement(ach.id)
                prefs.totalTickets = prefs.totalTickets + ach.rewardTickets
                audioManager.playAchievementFanfare()
                _uiState.update {
                    it.copy(
                        newlyUnlockedAchievement = ach,
                        totalTickets = prefs.totalTickets
                    )
                }
                viewModelScope.launch {
                    delay(3500L)
                    _uiState.update { if (it.newlyUnlockedAchievement?.id == ach.id) it.copy(newlyUnlockedAchievement = null) else it }
                }
            }
        }

        _uiState.update {
            it.copy(
                achievements = buildAchievementsList(),
                prizes = buildPrizesList()
            )
        }
    }

    private fun finishGame() {
        stopAllGameJobs()
        val state = _uiState.value

        prefs.saveHighScore(state.difficulty.name, state.score)
        val newTotalTickets = prefs.totalTickets + state.ticketsThisGame
        prefs.totalTickets = newTotalTickets

        audioManager.playVictoryFanfare()

        _uiState.update {
            it.copy(
                isPlaying = false,
                isGameOver = true,
                totalTickets = newTotalTickets,
                highScore = prefs.getHighScore(it.difficulty.name),
                moles = it.moles.map { m -> m.copy(state = MoleState.HIDDEN) }
            )
        }

        checkAchievements()
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun claimPrize(prizeId: String) {
        val prize = _uiState.value.prizes.find { it.id == prizeId } ?: return
        if (prize.isClaimed || _uiState.value.totalTickets < prize.costTickets) return

        prefs.totalTickets = prefs.totalTickets - prize.costTickets
        prefs.claimPrize(prizeId)
        audioManager.playStarChime()

        _uiState.update {
            it.copy(
                totalTickets = prefs.totalTickets,
                prizes = buildPrizesList()
            )
        }
    }

    fun returnToMenu() {
        stopAllGameJobs()
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.MENU,
                isPlaying = false,
                isGameOver = false,
                isPaused = false,
                activeMalletHole = null,
                moles = List(6) { index -> Mole(id = index) }
            )
        }
    }

    private fun stopAllGameJobs() {
        gameTimerJob?.cancel()
        moleSpawnerJob?.cancel()
        moleDismissJobs.values.forEach { it.cancel() }
        moleDismissJobs.clear()
    }

    override fun onCleared() {
        super.onCleared()
        stopAllGameJobs()
        audioManager.stopMusic()
    }
}
