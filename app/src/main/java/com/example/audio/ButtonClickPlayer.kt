package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Plays a short, cheerful "pop" sound for UI button presses.
 * The sound is synthesized, so no audio assets are needed.
 */
class ButtonClickPlayer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun play() {
        scope.launch {
            try {
                val sampleRate = 22050
                val totalSamples = (sampleRate * 0.09).toInt()
                val buffer = ShortArray(totalSamples)
                var phase = 0.0

                for (i in 0 until totalSamples) {
                    val progress = i.toDouble() / totalSamples
                    val freq = 700.0 + (1100.0 - 700.0) * progress
                    phase += 2.0 * Math.PI * freq / sampleRate

                    val attack = minOf(1.0, i / (sampleRate * 0.008))
                    val release = minOf(1.0, (totalSamples - i) / (sampleRate * 0.03))
                    val sample = sin(phase) * attack * release * 0.35

                    buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                }

                val track = AudioTrack.Builder()
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

                track.write(buffer, 0, buffer.size)
                track.play()

                delay(130L)
                track.release()
            } catch (_: Exception) {
                // Ignore audio errors gracefully
            }
        }
    }
}
