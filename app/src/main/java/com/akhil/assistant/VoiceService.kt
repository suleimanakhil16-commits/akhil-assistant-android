
    
private fun handleCommand(spokenText: String) {
    val command = spokenText
        .lowercase(Locale.ROOT)
        .trim()
        .replace(Regex("[^a-z0-9 ]"), " ")
        .replace(Regex("\\s+"), " ")

    updateNotification("You said: $spokenText")

    when (command) {
        "stop listening", "stop assistant" -> {
            speak("Stopping AKHIL Assistant")
            handler.postDelayed({ stopSelf() }, 1500)
            return
        }

        "whatsapp", "open whatsapp" ->
            openApp("com.whatsapp", "WhatsApp")

        "facebook", "open facebook" ->
            openApp("com.facebook.katana", "Facebook")

        "chrome", "open chrome" ->
            openApp("com.android.chrome", "Chrome")

        "chatgpt", "open chatgpt",
        "chat gpt", "open chat gpt" ->
            openApp("com.openai.chatgpt", "ChatGPT")

        "claude", "open claude" ->
            openApp("com.anthropic.claude", "Claude")

        "phone", "open phone",
        "dialer", "open dialer" ->
            openSystemScreen(
                Intent(Intent.ACTION_DIAL),
                "Phone"
            )

        "settings", "setting",
        "open settings", "open setting" ->
            openSystemScreen(
                Intent(android.provider.Settings.ACTION_SETTINGS),
                "Settings"
            )

        else -> {
            updateNotification("Command not recognized")
            speak("Sorry, I did not understand that command")
            restartListening()
        }
    }
}
