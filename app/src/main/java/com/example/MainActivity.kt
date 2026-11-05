package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.audio.ButtonClickPlayer
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.PrizesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                        WhacAMoleApp(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResumed()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onAppPaused()
    }
}

@Composable
fun WhacAMoleApp(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()
    val clickPlayer = remember { ButtonClickPlayer() }
    val click: () -> Unit = { if (state.sfxEnabled) clickPlayer.play() }

    when (state.currentScreen) {
        AppScreen.MENU -> MainMenuScreen(
            state = state,
            onSelectDifficulty = { click(); viewModel.selectDifficulty(it) },
            onStartGame = { click(); viewModel.startGame() },
            onOpenAchievements = { click(); viewModel.navigateTo(AppScreen.ACHIEVEMENTS) },
            onOpenPrizes = { click(); viewModel.navigateTo(AppScreen.PRIZES) },
            onToggleSfx = { click(); viewModel.toggleSfx() },
            onToggleMusic = { click(); viewModel.toggleMusic() }
        )

        AppScreen.GAME -> GameScreen(
            state = state,
            onWhackMole = viewModel::whackMole,
            onTapEmptySpace = viewModel::tapEmptySpace,
            onPauseToggle = { click(); viewModel.togglePause() },
            onReturnToMenu = { click(); viewModel.returnToMenu() },
            onToggleSfx = { click(); viewModel.toggleSfx() },
            onRestartGame = { click(); viewModel.startGame() }
        )

        AppScreen.ACHIEVEMENTS -> AchievementsScreen(
            state = state,
            onBack = { click(); viewModel.navigateTo(AppScreen.MENU) }
        )

        AppScreen.PRIZES -> PrizesScreen(
            state = state,
            onClaimPrize = { click(); viewModel.claimPrize(it) },
            onBack = { click(); viewModel.navigateTo(AppScreen.MENU) }
        )
    }
}
