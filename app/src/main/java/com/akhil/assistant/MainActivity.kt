package com.akhil.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var statusText: TextView

    companion object {
        private const val MICROPHONE_PERMISSION_REQUEST = 100
        private const val NOTIFICATION_PERMISSION_REQUEST = 101
    }

    private val backgroundColor = Color.rgb(9, 7, 24)
    private val cardColor = Color.rgb(28, 22, 54)
    private val accentColor = Color.rgb(183, 120, 255)
    private val whiteColor = Color.WHITE
    private val mutedColor = Color.rgb(183, 174, 207)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(backgroundColor)
            isFillViewport = true
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(28))
        }

        layout.addView(makeText("AKHIL", 30f, whiteColor, true))
        layout.addView(makeText("YOUR PERSONAL AI ASSISTANT", 11f, mutedColor, true), marginParams(top = 4, bottom = 24))

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(24), dp(20), dp(26))
            background = rounded(cardColor, 28, Color.rgb(91, 63, 133))
        }
        hero.addView(makeText("MEET YOUR ASSISTANT", 12f, accentColor, true))

        // Temporary portrait placeholder; the final realistic avatar image will be added as a drawable asset.
        val avatar = TextView(this).apply {
            text = "👩🏻"
            textSize = 66f
            gravity = Gravity.CENTER
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(111, 63, 171), Color.rgb(48, 31, 87))
            ).apply {
                shape = GradientDrawable.OVAL
                setStroke(dp(3), accentColor)
            }
        }
        hero.addView(avatar, LinearLayout.LayoutParams(dp(154), dp(154)).apply {
            topMargin = dp(18)
            bottomMargin = dp(18)
        })

        hero.addView(makeText("AKHIL ASSISTANT", 19f, whiteColor, true))
        statusText = makeText("READY TO LISTEN", 13f, accentColor, true)
        hero.addView(statusText, marginParams(top = 9))
        val description = makeText("Speak naturally. I'm ready for your commands.", 13f, mutedColor, false)
        description.gravity = Gravity.CENTER
        hero.addView(description, marginParams(top = 8))
        layout.addView(hero)

        val startButton = makeButton("🎙   START LISTENING", accentColor, Color.rgb(24, 12, 40))
        startButton.setOnClickListener { requestMicrophonePermission() }
        layout.addView(startButton, marginParams(top = 22))

        val stopButton = makeButton("STOP LISTENING", cardColor, whiteColor)
        stopButton.background = rounded(cardColor, 16, Color.rgb(91, 75, 125))
        stopButton.setOnClickListener {
            stopService(Intent(this, VoiceService::class.java))
            statusText.text = "ASSISTANT STOPPED"
        }
        layout.addView(stopButton, marginParams(top = 11))

        val commandsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = rounded(cardColor, 22, Color.rgb(72, 55, 104))
        }
        commandsCard.addView(makeText("QUICK COMMANDS", 13f, accentColor, true))
        val commands = listOf("WhatsApp", "Chrome", "Facebook", "ChatGPT", "Settings")
        commands.forEach { name ->
            val item = TextView(this).apply {
                text = "✦    Open $name"
                textSize = 14f
                setTextColor(whiteColor)
                setPadding(dp(12), dp(12), dp(12), dp(12))
                background = rounded(Color.rgb(38, 30, 68), 13)
            }
            commandsCard.addView(item, marginParams(top = 9))
        }
        layout.addView(commandsCard, marginParams(top = 22))

        val footer = makeText("YOUR VOICE  •  YOUR CONTROL", 10f, mutedColor, true)
        footer.gravity = Gravity.CENTER
        layout.addView(footer, marginParams(top = 24))

        scrollView.addView(layout)
        setContentView(scrollView)

        MorningAlarmReceiver.schedule(this)

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST)
        }
    }

    private fun requestMicrophonePermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), MICROPHONE_PERMISSION_REQUEST)
        } else startVoiceService()
    }

    private fun startVoiceService() {
        val serviceIntent = Intent(this, VoiceService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(serviceIntent)
        else startService(serviceIntent)
        statusText.text = "VOICE ASSISTANT IS RUNNING"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MICROPHONE_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) startVoiceService()
            else statusText.text = "MICROPHONE PERMISSION NEEDED"
        }
    }

    private fun makeText(value: String, size: Float, color: Int, bold: Boolean): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }

    private fun makeButton(label: String, color: Int, textColor: Int): Button =
        Button(this).apply {
            text = label
            textSize = 14f
            isAllCaps = false
            setTextColor(textColor)
            typeface = Typeface.DEFAULT_BOLD
            background = rounded(color, 16)
            minHeight = dp(56)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }

    private fun rounded(color: Int, radius: Int, strokeColor: Int? = null): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (strokeColor != null) setStroke(dp(1), strokeColor)
        }

    private fun marginParams(top: Int = 0, bottom: Int = 0): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(top)
            bottomMargin = dp(bottom)
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
