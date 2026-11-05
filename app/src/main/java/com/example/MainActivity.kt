package com.example

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ads.AdManager
import com.example.ads.ConsentManager
import com.example.audio.ButtonClickPlayer
import com.example.ui.components.BannerAdView
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

        // Ads are initialized only after the user's consent status allows it.
        ConsentManager.gatherConsent(this) {
            AdManager.initialize(applicationContext)
        }

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

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun WhacAMoleApp(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()
    val privacyOptionsRequired by ConsentManager.privacyOptionsRequired.collectAsState()
    val clickPlayer = remember { ButtonClickPlayer() }
    val click: () -> Unit = { if (state.sfxEnabled) clickPlayer.play() }

    val activity = LocalContext.current.findActivity()
    var gamesFinished by rememberSaveable { mutableIntStateOf(0) }

    // Runs [action]; if the player is leaving the results screen and it is time for an ad,
    // an interstitial is shown first (every AdManager.GAMES_PER_INTERSTITIAL games).
    val afterGameOver: (() -> Unit) -> Unit = { action ->
        if (state.isGameOver && activity != null) {
            gamesFinished += 1
            if (gamesFinished % AdManager.GAMES_PER_INTERSTITIAL == 0) {
                AdManager.showInterstitial(activity, action)
            } else {
                action()
            }
        } else {
            action()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
                    onReturnToMenu = { click(); afterGameOver { viewModel.returnToMenu() } },
                    onToggleSfx = { click(); viewModel.toggleSfx() },
                    onRestartGame = { click(); afterGameOver { viewModel.startGame() } }
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

        // Privacy options entry point (required by Google when the consent form applies).
        if (state.currentScreen == AppScreen.MENU && privacyOptionsRequired && activity != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFCC80)),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = {
                        click()
                        ConsentManager.showPrivacyOptionsForm(activity)
                    },
                    modifier = Modifier.testTag("privacy_options_button")
                ) {
                    Text(
                        text = "🔒 Opciones de privacidad",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6D4C41)
                    )
                }
            }
        }

        // Banner on menu, achievements and prizes. Never during the game.
        if (state.currentScreen != AppScreen.GAME) {
            BannerAdView(modifier = Modifier.fillMaxWidth())
        }
    }
}
