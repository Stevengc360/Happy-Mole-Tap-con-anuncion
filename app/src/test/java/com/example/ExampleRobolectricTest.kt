package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.ArcadeAudioManager
import com.example.audio.SoundManager
import com.example.model.Difficulty
import com.example.model.MoleType
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Golpea al Topo", appName)
  }

  @Test
  fun `difficulty levels have valid timings`() {
    assertTrue(Difficulty.FACIL.moleVisibleDurationMs > Difficulty.MEDIO.moleVisibleDurationMs)
    assertTrue(Difficulty.MEDIO.moleVisibleDurationMs > Difficulty.DIFICIL.moleVisibleDurationMs)
  }

  @Test
  fun `mole types provide points and tickets`() {
    assertTrue(MoleType.NORMAL.points > 0)
    assertTrue(MoleType.CONSTRUCTOR.points > MoleType.NORMAL.points)
    assertTrue(MoleType.DORADO.points > MoleType.CONSTRUCTOR.points)
    assertTrue(MoleType.DORADO.tickets > MoleType.NORMAL.tickets)
  }

  @Test
  fun `sound manager upbeat hit and miss sounds execute safely`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val soundManager: SoundManager = ArcadeAudioManager(context)

    assertTrue(soundManager.isSfxEnabled)
    soundManager.playUpbeatHitSound(combo = 2, isSpecial = false)
    soundManager.playMissSound()
    soundManager.playPopUpSound()

    soundManager.isSfxEnabled = false
    assertFalse(soundManager.isSfxEnabled)
    soundManager.playUpbeatHitSound(combo = 5, isSpecial = true)
    soundManager.playMissSound()
  }

  @Test
  fun `game viewModel registers misses on empty taps`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = GameViewModel(app)
    viewModel.startGame()

    assertEquals(0, viewModel.uiState.value.misses)

    // Tapping empty space
    viewModel.tapEmptySpace()
    assertEquals(1, viewModel.uiState.value.misses)
    assertEquals(0, viewModel.uiState.value.combo)

    // Tapping empty hole
    viewModel.whackMole(0)
    // If hole 0 is hidden, it counts as a miss
    if (viewModel.uiState.value.moles[0].state != com.example.model.MoleState.UP) {
      assertEquals(2, viewModel.uiState.value.misses)
    }
  }
}
