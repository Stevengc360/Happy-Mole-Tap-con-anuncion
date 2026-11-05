package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.asin
import kotlin.math.sin

/**
 * Sound Management System interface for arcade audio.
 */
interface SoundManager {
    var isSfxEnabled: Boolean
    var isMusicEnabled: Boolean

    fun playUpbeatHitSound(combo: Int = 0, isSpecial: Boolean = false)
    fun playMissSound()
    fun playWhackSound()
    fun playPopUpSound()
    fun playStarChime()
    fun playTicketSound()
    fun playAchievementFanfare()
    fun playVictoryFanfare()
    fun startBackgroundMusic()
    fun stopMusic()
}

/**
 * Arcade Audio & Haptics Engine implementing SoundManager.
 * Synthesizes cheerful sound effects and happy music using AudioTrack so it plays
 * reliably offline with zero latency and without external asset loading delays.
 */
class ArcadeAudioManager(private val context: Context) : SoundManager {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    override var isSfxEnabled: Boolean = true
    override var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                stopMusic()
            }
        }

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null

    // Generates static PCM audio tone
    private fun playTone(
        frequencies: List<Float>,
        durationMs: Int,
        volume: Float = 0.5f,
        waveType: Int = 0 // 0: sine, 1: triangle/warm
    ) {
        if (!isSfxEnabled) return

        audioScope.launch {
            try {
                val sampleRate = 22050
                val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                if (totalSamples <= 0) return@launch
                val buffer = ShortArray(totalSamples)

                for (i in 0 until totalSamples) {
                    val time = i.toDouble() / sampleRate
                    var sampleVal = 0.0

                    // Fade in/out envelope to prevent clicks
                    val envelope = when {
                        i < sampleRate * 0.01 -> i / (sampleRate * 0.01)
                        i > totalSamples - sampleRate * 0.02 -> (totalSamples - i) / (sampleRate * 0.02)
                        else -> 1.0
                    }

                    for (f in frequencies) {
                        val angle = 2.0 * Math.PI * f * time
                        sampleVal += if (waveType == 1) {
                            (2.0 / Math.PI) * asin(sin(angle))
                        } else {
                            sin(angle)
                        }
                    }

                    sampleVal = (sampleVal / frequencies.size) * envelope * volume
                    buffer[i] = (sampleVal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                delay(durationMs + 40L)
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio errors gracefully
            }
        }
    }

    // Generates continuous frequency slide (whistle/whoosh/slide)
    private fun playSweep(
        startFreq: Float,
        endFreq: Float,
        durationMs: Int,
        volume: Float = 0.4f,
        waveType: Int = 1
    ) {
        if (!isSfxEnabled) return

        audioScope.launch {
            try {
                val sampleRate = 22050
                val totalDurationSec = durationMs / 1000.0
                val totalSamples = (sampleRate * totalDurationSec).toInt()
                if (totalSamples <= 0) return@launch
                val buffer = ShortArray(totalSamples)

                var phase = 0.0
                for (i in 0 until totalSamples) {
                    val progress = i.toDouble() / totalSamples
                    val currentFreq = startFreq + (endFreq - startFreq) * progress

                    phase += 2.0 * Math.PI * currentFreq / sampleRate

                    val envelope = when {
                        i < sampleRate * 0.015 -> i / (sampleRate * 0.015)
                        i > totalSamples - sampleRate * 0.025 -> (totalSamples - i) / (sampleRate * 0.025)
                        else -> 1.0
                    }

                    val sampleVal = if (waveType == 1) {
                        (2.0 / Math.PI) * asin(sin(phase))
                    } else {
                        sin(phase)
                    } * envelope * volume

                    buffer[i] = (sampleVal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                delay(durationMs + 30L)
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio errors gracefully
            }
        }
    }

    /**
     * Upbeat sound effect played when a mole is successfully hit!
     * Produces a bouncy, punchy attack followed by a rising cheerful chord
     * that ascends dynamically with the combo multiplier.
     */
    override fun playUpbeatHitSound(combo: Int, isSpecial: Boolean) {
        if (!isSfxEnabled) return
        vibrate(35)

        audioScope.launch {
            // Pentatonic scale pitches for musical reward
            val scalePitches = listOf(523.25f, 587.33f, 659.25f, 783.99f, 880.00f, 1046.50f)
            val basePitch = scalePitches[combo.coerceIn(0, scalePitches.size - 1)]

            // 1. Initial punchy comic thud
            playTone(listOf(280f, 150f), 45, volume = 0.55f, waveType = 1)
            delay(30)

            // 2. Upbeat bright chime notes (major interval)
            playTone(listOf(basePitch, basePitch * 1.26f), 70, volume = 0.45f, waveType = 0)

            // 3. Extra sparkle if special mole (golden/crown or high combo)
            if (isSpecial || combo >= 3) {
                delay(50)
                playTone(listOf(basePitch * 1.5f, basePitch * 2f), 80, volume = 0.4f, waveType = 0)
            }
        }
    }

    /**
     * Cartoon 'miss' sound effect played when the player taps empty space or an empty hole.
     * Features a comical downward swoosh (whoosh) followed by a soft hollow thud.
     */
    override fun playMissSound() {
        if (!isSfxEnabled) return
        vibrate(18) // Light tactile feedback on miss

        audioScope.launch {
            // Comical downward air whoosh (500Hz -> 130Hz)
            playSweep(startFreq = 480f, endFreq = 140f, durationMs = 110, volume = 0.38f, waveType = 1)
            delay(90)
            // Soft hollow 'piff/dull boing' sound
            playTone(listOf(160f, 190f), 50, volume = 0.28f, waveType = 0)
        }
    }

    /** Backward-compatible standard whack punch sound */
    override fun playWhackSound() {
        playUpbeatHitSound(combo = 0, isSpecial = false)
    }

    /** Cute mole popping up sound (boing!) */
    override fun playPopUpSound() {
        if (!isSfxEnabled) return
        audioScope.launch {
            playSweep(startFreq = 280f, endFreq = 540f, durationMs = 75, volume = 0.25f, waveType = 0)
        }
    }

    /** Golden mole or special hit star chime */
    override fun playStarChime() {
        if (!isSfxEnabled) return
        vibrate(60)
        audioScope.launch {
            val notes = listOf(523f, 659f, 784f, 1046f)
            for (note in notes) {
                playTone(listOf(note), 60, volume = 0.35f, waveType = 0)
                delay(40)
            }
        }
    }

    /** Ticket dispensed sound */
    override fun playTicketSound() {
        if (!isSfxEnabled) return
        audioScope.launch {
            playTone(listOf(880f, 1175f), 45, volume = 0.3f, waveType = 1)
        }
    }

    /** Achievement unlocked fanfare */
    override fun playAchievementFanfare() {
        if (!isSfxEnabled) return
        vibrate(100)
        audioScope.launch {
            val fanfare = listOf(440f, 554f, 659f, 880f)
            for (freq in fanfare) {
                playTone(listOf(freq), 90, volume = 0.45f, waveType = 1)
                delay(70)
            }
        }
    }

    /** Game Over cheer / victory fanfare */
    override fun playVictoryFanfare() {
        if (!isSfxEnabled) return
        audioScope.launch {
            val notes = listOf(392f, 523f, 659f, 784f, 1046f)
            for (note in notes) {
                playTone(listOf(note), 120, volume = 0.4f, waveType = 1)
                delay(100)
            }
        }
    }

    /** Background cheerful nursery arcade music loop */
    override fun startBackgroundMusic() {
        if (!isMusicEnabled || musicJob?.isActive == true) return

        musicJob = audioScope.launch {
            val melody = listOf(
                523f to 200L, 659f to 200L, 784f to 200L, 659f to 200L,
                880f to 200L, 784f to 200L, 659f to 350L,
                587f to 200L, 659f to 200L, 587f to 200L, 523f to 400L,
                659f to 200L, 784f to 200L, 880f to 200L, 1046f to 350L,
                784f to 200L, 659f to 200L, 523f to 450L
            )

            while (isActive && isMusicEnabled) {
                for ((freq, dur) in melody) {
                    if (!isActive || !isMusicEnabled) break
                    playTone(listOf(freq, freq * 0.5f), dur.toInt(), volume = 0.12f, waveType = 0)
                    delay(dur + 30L)
                }
                delay(500L)
            }
        }
    }

    override fun stopMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Ignore vibration error
        }
    }
}
