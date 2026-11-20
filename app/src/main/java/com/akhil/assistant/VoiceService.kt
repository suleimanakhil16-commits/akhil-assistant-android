package com.akhil.assistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null

    private val handler = Handler(Looper.getMainLooper())

    private var isStopping = false
    private var isListening = false
    private var ttsReady = false
    private var pendingSpeech: String? = null

    companion object {
        private const val CHANNEL_ID = "akhil_voice"
        private const val NOTIFICATION_ID = 1001
        private const val RESTART_DELAY_MS = 1000L
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            buildNotification("AKHIL Assistant is starting")
        )

        initializeSpeech()
    }

    private fun initializeSpeech() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)

                if (result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    ttsReady = true

                    textToSpeech?.setSpeechRate(1.0f)
                    textToSpeech?.setPitch(1.0f)

                    pendingSpeech?.let {
                        pendingSpeech = null
                        speak(it)
                    }
                } else {
                    updateNotification("English voice is unavailable")
                }
            } else {
                updateNotification("Speech engine failed to start")
            }
        }
    }

    private fun speak(message: String) {
        if (isStopping) return

        if (!ttsReady) {
            pendingSpeech = message
            return
        }

        textToSpeech?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "AKHIL_REPLY"
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        isStopping = false
        startSpeechRecognition()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AKHIL Voice Assistant",
                NotificationManager.IMPORTANCE_LOW
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(message: String): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("AKHIL Assistant")
            .setContentText(message)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(message: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(message))
    }

    private fun startSpeechRecognition() {
        if (isStopping) return

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            updateNotification("Speech recognition is unavailable")
            return
        }

        try {
            if (speechRecognizer == null) {
                speechRecognizer =
                    SpeechRecognizer.createSpeechRecognizer(this)

                speechRecognizer?.setRecognitionListener(
                    object : RecognitionListener {

                        override fun onReadyForSpeech(params: Bundle?) {
                            updateNotification("Listening. Say a command.")
                        }

                        override fun onBeginningOfSpeech() {
                            updateNotification("I heard you. Processing.")
                        }

                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {}

                        override fun onError(error: Int) {
                            isListening = false
                            updateNotification(recognitionErrorMessage(error))
                            restartListening()
                        }

                        override fun onResults(results: Bundle?) {
                            isListening = false
                            val candidates = results
                                ?.getStringArrayList(
                                    SpeechRecognizer.RESULTS_RECOGNITION
                                )
                                .orEmpty()

                            val spokenText = candidates.firstOrNull {
                                resolveCommand(it) != null
                            } ?: candidates.firstOrNull()

                            if (!spokenText.isNullOrBlank()) {
                                handleCommand(spokenText)
                            } else {
                                updateNotification("No speech detected. Please try again.")
                                speak("Please repeat your command")
                                restartListening()
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {}
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    }
                )
            }

            val recognitionIntent =
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE,
                        Locale.US.toLanguageTag()
                    )
                    putExtra(
                        RecognizerIntent.EXTRA_MAX_RESULTS,
                        3
                    )
                }

            if (!isListening) {
                isListening = true
                speechRecognizer?.startListening(recognitionIntent)
            }

        } catch (exception: Exception) {
            isListening = false
            updateNotification("Could not start listening")
            restartListening()
        }
    }

    private fun recognitionErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Microphone audio error. Check microphone access."
            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network problem. Retrying voice recognition."
            SpeechRecognizer.ERROR_NO_MATCH,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No clear speech detected. Listening again."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice recognizer is busy. Retrying."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is missing."
            else -> "Voice recognition error ($error). Retrying."
        }
    }

    private fun restartListening() {
        handler.removeCallbacksAndMessages(null)

        if (!isStopping) {
            handler.postDelayed(
                { startSpeechRecognition() },
                RESTART_DELAY_MS
            )
        }
    }

    private fun normalizeCommand(text: String): String {
        return text.lowercase(Locale.ROOT)
            .trim()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
    }

    private fun resolveCommand(spokenText: String): String? {
        var command = normalizeCommand(spokenText)

        command = command
            .replace(Regex("^(please |hey )+"), "")
            .replace(Regex("^(akhil )+"), "")
            .trim()

        if (command in setOf("stop listening", "stop assistant", "stop akhil")) {
            return "stop"
        }

        command = command.replace(
            Regex("^(please )?(open|launch|start|show|go to|switch to) "),
            ""
        ).trim()

        return when (command) {
            "whatsapp", "whats app" -> "whatsapp"
            "facebook", "face book" -> "facebook"
            "chrome", "google chrome" -> "chrome"
            "chatgpt", "chat gpt" -> "chatgpt"
            "claude" -> "claude"
            "phone", "dialer", "make a call" -> "phone"
            "settings", "setting" -> "settings"
            else -> null
        }
    }

    private fun handleCommand(spokenText: String) {
        updateNotification("You said: $spokenText")

        when (resolveCommand(spokenText)) {
            "stop" -> {
                isStopping = true
                isListening = false
                speak("Stopping AKHIL Assistant")
                handler.postDelayed({ stopSelf() }, 1500)
                return
            }

            "whatsapp" -> openApp("com.whatsapp", "WhatsApp")
            "facebook" -> openApp("com.facebook.katana", "Facebook")
            "chrome" -> openApp("com.android.chrome", "Chrome")
            "chatgpt" -> openApp("com.openai.chatgpt", "ChatGPT")
            "claude" -> openApp("com.anthropic.claude", "Claude")
            "phone" -> openSystemScreen(Intent(Intent.ACTION_DIAL), "Phone")
            "settings" -> openSystemScreen(
                Intent(android.provider.Settings.ACTION_SETTINGS),
                "Settings"
            )

            else -> {
                updateNotification("Command not recognized: $spokenText")
                speak("Sorry, I did not understand that command")
                restartListening()
            }
        }
    }

    private fun openApp(packageName: String, appName: String) {
        val launchIntent =
            packageManager.getLaunchIntentForPackage(packageName)

        if (launchIntent == null) {
            updateNotification("$appName is not installed")
            speak("$appName is not installed")
            restartListening()
            return
        }

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            startActivity(launchIntent)
            updateNotification("Opening $appName")
            speak("Opening $appName")
            restartListening()
        } catch (exception: Exception) {
            updateNotification("Could not open $appName")
            speak("Could not open $appName")
            restartListening()
        }
    }

    private fun openSystemScreen(
        intent: Intent,
        screenName: String
    ) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            startActivity(intent)
            updateNotification("Opening $screenName")
            speak("Opening $screenName")
            restartListening()
        } catch (exception: Exception) {
            updateNotification("Could not open $screenName")
            speak("Could not open $screenName")
            restartListening()
        }
    }

    override fun onDestroy() {
        isStopping = true

        handler.removeCallbacksAndMessages(null)

        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null

        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
