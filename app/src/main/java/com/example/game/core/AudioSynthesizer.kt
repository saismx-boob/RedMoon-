package com.example.game.core

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural low-latency arcade audio synthesizer for retro 90s fighting game sound effects.
 */
class AudioSynthesizer {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var isMuted = false
    private val sampleRate = 22050

    fun setMuted(muted: Boolean) {
        this.isMuted = muted
    }

    fun isMuted(): Boolean = isMuted

    fun playSound(name: String) {
        if (isMuted) return
        scope.launch {
            try {
                val samples = when (name) {
                    "hit_light" -> generateNoiseBurst(0.08f, 1200f)
                    "hit_heavy" -> generateHeavyImpact(0.22f)
                    "slash" -> generateSlashWhoosh(0.18f)
                    "block" -> generateBlockClang(0.12f)
                    "super_activation" -> generateSuperBoom(0.45f)
                    "projectile" -> generateEnergyProjectile(0.25f)
                    "jump" -> generateSweep(0.12f, 150f, 400f)
                    "dash" -> generateNoiseBurst(0.10f, 600f)
                    "ko" -> generateKoFanfare()
                    "round_start" -> generateSweep(0.25f, 220f, 660f)
                    else -> generateNoiseBurst(0.08f, 900f)
                }
                playBuffer(samples)
            } catch (_: Exception) {
                // Ignore audio generation failures gracefully
            }
        }
    }

    private fun playBuffer(samples: ShortArray) {
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
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(samples, 0, samples.size)
        audioTrack.play()
        
        // Release after playback
        val durationMs = (samples.size * 1000L) / sampleRate + 50L
        scope.launch {
            kotlinx.coroutines.delay(durationMs)
            audioTrack.release()
        }
    }

    private fun generateNoiseBurst(durationSec: Float, cutoff: Float): ShortArray {
        val count = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(count)
        var last = 0f
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            val envelope = (1f - progress) * (1f - progress)
            val white = (Random.nextFloat() * 2f - 1f)
            last += (white - last) * (cutoff / sampleRate).coerceIn(0.01f, 0.99f)
            buffer[i] = (last * envelope * 18000f).toInt().toShort()
        }
        return buffer
    }

    private fun generateHeavyImpact(durationSec: Float): ShortArray {
        val count = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            val envelope = (1f - progress) * (1f - progress)
            val pitch = 140f * (1f - progress * 0.7f)
            val tone = sin(2.0 * Math.PI * pitch * i / sampleRate).toFloat()
            val noise = (Random.nextFloat() * 2f - 1f) * (1f - progress) * 0.6f
            buffer[i] = ((tone + noise) * envelope * 24000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateSlashWhoosh(durationSec: Float): ShortArray {
        val count = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            val envelope = sin(progress * Math.PI.toFloat())
            val freq = 450f + progress * 800f
            val tone = sin(2.0 * Math.PI * freq * i / sampleRate).toFloat()
            val noise = (Random.nextFloat() * 2f - 1f) * 0.4f
            buffer[i] = ((tone + noise) * envelope * 19000f).toInt().toShort()
        }
        return buffer
    }

    private fun generateBlockClang(durationSec: Float): ShortArray {
        val count = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            val envelope = kotlin.math.exp(-progress * 14f)
            val tone1 = sin(2.0 * Math.PI * 920.0 * i / sampleRate).toFloat()
            val tone2 = sin(2.0 * Math.PI * 1440.0 * i / sampleRate).toFloat()
            buffer[i] = ((tone1 * 0.6f + tone2 * 0.4f) * envelope * 22000f).toInt().toShort()
        }
        return buffer
    }

    private fun generateSuperBoom(durationSec: Float): ShortArray {
        val count = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            val envelope = (1f - progress)
            val sweep = 380f * (1f - progress * 0.85f)
            val tone = sin(2.0 * Math.PI * sweep * i / sampleRate).toFloat()
            val sub = sin(2.0 * Math.PI * (sweep * 0.5) * i / sampleRate).toFloat() * 0.8f
            val crack = if (i < count * 0.15f) (Random.nextFloat() * 2f - 1f) * 0.8f else 0f
            buffer[i] = ((tone + sub + crack) * envelope * 26000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateEnergyProjectile(durationSec: Float): ShortArray {
        val count = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            val envelope = (1f - progress)
            val freq = 220f + sin(progress * 24.0) * 80.0
            val tone = sin(2.0 * Math.PI * freq * i / sampleRate).toFloat()
            buffer[i] = (tone * envelope * 18000f).toInt().toShort()
        }
        return buffer
    }

    private fun generateSweep(durationSec: Float, startFreq: Float, endFreq: Float): ShortArray {
        val count = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            val envelope = sin(progress * Math.PI.toFloat())
            val freq = startFreq + (endFreq - startFreq) * progress
            val tone = sin(2.0 * Math.PI * freq * i / sampleRate).toFloat()
            buffer[i] = (tone * envelope * 18000f).toInt().toShort()
        }
        return buffer
    }

    private fun generateKoFanfare(): ShortArray {
        val durationSec = 0.6f
        val count = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(count)
        val chord = doubleArrayOf(330.0, 392.0, 523.0, 660.0)
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            val envelope = (1f - progress) * (1f - progress)
            var sample = 0f
            for (f in chord) {
                sample += sin(2.0 * Math.PI * f * i / sampleRate).toFloat() / chord.size
            }
            buffer[i] = (sample * envelope * 22000f).toInt().toShort()
        }
        return buffer
    }
}
