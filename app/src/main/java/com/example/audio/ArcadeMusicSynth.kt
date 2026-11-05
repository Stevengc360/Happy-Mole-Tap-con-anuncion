package com.example.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * Renders an upbeat 8-bar arcade chiptune loop (150 BPM) into PCM samples:
 * pulse-wave lead melody, triangle bass, kick, snare and hi-hat.
 */
object ArcadeMusicSynth {

    const val SAMPLE_RATE = 22050
    private const val BPM = 150

    // MIDI notes, 8 eighth-notes per bar. Progression: C - Am - F - G - C - Am - F - G
    private val LEAD = intArrayOf(
        72, 76, 79, 76, 84, 79, 76, 79,
        69, 72, 76, 72, 81, 76, 72, 76,
        77, 81, 84, 81, 89, 84, 81, 84,
        79, 83, 86, 83, 91, 86, 83, 86,
        84, 79, 76, 79, 84, 88, 86, 84,
        81, 76, 72, 76, 81, 84, 83, 81,
        77, 81, 84, 81, 89, 88, 86, 84,
        79, 83, 86, 91, 89, 88, 86, 83
    )

    private val BASS_ROOTS = intArrayOf(48, 45, 41, 43, 48, 45, 41, 43)

    fun render(): ShortArray {
        val stepSamples = (SAMPLE_RATE * 60.0 / BPM / 2.0).toInt()
        val steps = LEAD.size
        val mix = DoubleArray(stepSamples * steps)
        val rnd = Random(42)

        for (s in 0 until steps) {
            val start = s * stepSamples
            val bar = s / 8
            val pos = s % 8
            val noteLen = (stepSamples * 0.9).toInt()

            addPulse(mix, start, midiToFreq(LEAD[s]), noteLen, 0.16, 0.25)

            if (pos % 2 == 0) {
                val root = BASS_ROOTS[bar] + if (pos == 2 || pos == 6) 12 else 0
                addTriangle(mix, start, midiToFreq(root), noteLen, 0.22)
            }

            if (pos == 0 || pos == 4) addKick(mix, start, 0.32)
            if (pos == 2 || pos == 6) addNoise(mix, start, (SAMPLE_RATE * 0.09).toInt(), 0.12, 30.0, rnd)
            if (pos % 2 == 1) addNoise(mix, start, (SAMPLE_RATE * 0.03).toInt(), 0.05, 80.0, rnd)
        }

        return ShortArray(mix.size) { i ->
            (mix[i].coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun midiToFreq(note: Int): Double = 440.0 * 2.0.pow((note - 69) / 12.0)

    private fun addPulse(mix: DoubleArray, start: Int, freq: Double, len: Int, amp: Double, duty: Double) {
        for (i in 0 until len) {
            val idx = start + i
            if (idx >= mix.size) break
            val t = i.toDouble() / SAMPLE_RATE
            val phase = (freq * t) % 1.0
            val v = if (phase < duty) 1.0 else -1.0
            val attack = minOf(1.0, i / (SAMPLE_RATE * 0.004))
            val release = minOf(1.0, (len - i) / (SAMPLE_RATE * 0.01))
            mix[idx] += v * amp * attack * release * exp(-t * 4.0)
        }
    }

    private fun addTriangle(mix: DoubleArray, start: Int, freq: Double, len: Int, amp: Double) {
        for (i in 0 until len) {
            val idx = start + i
            if (idx >= mix.size) break
            val t = i.toDouble() / SAMPLE_RATE
            val phase = (freq * t) % 1.0
            val v = 4.0 * abs(phase - 0.5) - 1.0
            val attack = minOf(1.0, i / (SAMPLE_RATE * 0.005))
            val release = minOf(1.0, (len - i) / (SAMPLE_RATE * 0.015))
            mix[idx] += v * amp * attack * release
        }
    }

    private fun addKick(mix: DoubleArray, start: Int, amp: Double) {
        val len = (SAMPLE_RATE * 0.13).toInt()
        var phase = 0.0
        for (i in 0 until len) {
            val idx = start + i
            if (idx >= mix.size) break
            val progress = i.toDouble() / len
            val freq = 150.0 - 100.0 * progress
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val t = i.toDouble() / SAMPLE_RATE
            mix[idx] += sin(phase) * amp * exp(-t * 25.0)
        }
    }

    private fun addNoise(mix: DoubleArray, start: Int, len: Int, amp: Double, decay: Double, rnd: Random) {
        for (i in 0 until len) {
            val idx = start + i
            if (idx >= mix.size) break
            val t = i.toDouble() / SAMPLE_RATE
            mix[idx] += (rnd.nextDouble() * 2.0 - 1.0) * amp * exp(-t * decay)
        }
    }
}
