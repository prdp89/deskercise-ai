package com.ai.app.move.deskercise.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import java.util.*

/**
 * Helper class to manage the Audio of the application.
 */
class AudioManagerService(cntx: Context) {

    var context = cntx

    init {
        setContext(context)
        tts = initializeTTS()
        sharedAudioPlayer = this
    }

    companion object {

        // / Singleton Audio manager.
        // / Main synthesizer used for Text-to-Speech.
        // / Main Audio player to play sound files.
        // / Plays .mp3 files using the `AVAudioPlayer`
        // /
        // / - parameters:
        // /     - url: The file path of the sound file to be played
        // / Speaks the given text string.
        // /
        // / - parameters:
        // /     - text: Text to be spoken

        private var audioPlayer: MediaPlayer? = null
        lateinit var tts: TextToSpeech

        // Todo: Address the memory leak warning and refactor code
        @SuppressLint("StaticFieldLeak")
        var _context: Context? = null

        @SuppressLint("StaticFieldLeak")
        lateinit var sharedAudioPlayer: AudioManagerService

        fun setContext(cntx: Context?) {
            _context = cntx
        }

        private fun initializeTTS(): TextToSpeech {
            return TextToSpeech(_context) {
                if (it == TextToSpeech.SUCCESS) {
                    tts.language = Locale.ENGLISH
                    tts.setSpeechRate(0.97f)
                }
            }
        }

        fun speakText(text: String, speakTillComplete: Boolean = false) {
            if (tts.isSpeaking) {
                // speakTillComplete flag is primarily to prevent the TextToSpeech program from interrupting itself
                // for certain exercises (i.e. NeckStretchRepetitionInProgressClassifier)
                // that tells the user to "move back" if certain number of landmarks are missing.
                // the speakTillComplete flag allows the TextToSpeech program to complete saying "move back" without interrupting itself
                // while the condition is true and the flag is set to true
                if (!speakTillComplete) {
                    tts.stop()
                }
            }

            if (_context != null) {
                if (!speakTillComplete || (speakTillComplete && !tts.isSpeaking)) {
                    tts.run { return@run this.speak(text, TextToSpeech.QUEUE_ADD, null) }
                }
            }
        }

        // Stop all TextToSpeech narration and sfx audio being played
        fun stopAll() {
            if (_context != null) {
                if (tts.isSpeaking) {
                    tts.stop()
                }

                val player = audioPlayer
                if (player != null) {
                    if (player.isPlaying) {
                        player.stop()
                    }
                }
            }
        }

        fun exit() {
            stopAll()
            setContext(null)
        }

        fun playStretchGoalReachedSFX() {
            // Files are within the assets folder
            play("Audio/Moves/Stretch/StretchGoalReached.mp3")
        }

        fun playStretchCompletionSFX() {
            // Files are within the assets folder
            play("Audio/Moves/Stretch/StretchCompletion.mp3")
        }

        fun playGoodJobCompletingAllExercisesNarration() {
            speakText(text = TextToSpeechText.goodJobCompletingAllExercises)
        }

        private fun play(relative_path_string: String) {
            try {
                if (_context != null) {
                    val afd: AssetFileDescriptor =
                        _context?.resources!!.assets!!.openFd(relative_path_string)
                    audioPlayer = MediaPlayer()
                    audioPlayer!!.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    audioPlayer!!.prepare()
                    audioPlayer!!.start()
                }
            } catch (e: Exception) {
                println("Sound Play Error -> $e")
            }
        }
    }
}
