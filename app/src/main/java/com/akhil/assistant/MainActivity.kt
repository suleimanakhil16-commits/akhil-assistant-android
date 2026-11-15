
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
import android.view.View
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

    private val backgroundColor = Color.rgb(10, 17, 31)
    private val cardColor = Color.rgb(20, 31, 50)
    private val accentColor = Color.rgb(0, 205, 170)
    private val whiteColor = Color.WHITE
    private val mutedColor = Color.rgb(160, 175, 195)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(backgroundColor)
            isFillViewport = true
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(30), dp(22), dp(30))
        }

        // Header
        val title = makeText("AKHIL", 30f, whiteColor, true)
        val subtitle = makeText(
            "YOUR PERSONAL AI ASSISTANT",
            12f,
            mutedColor,
            true
        )

        layout.addView(title)
        layout.addView(subtitle, marginParams(top = 5, bottom = 25))

        // Main assistant card
        val assistantCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(28), dp(20), dp(28))
            background = rounded(cardColor, 26)
        }

        val assistantLabel = makeText(
            "VOICE ASSISTANT",
            13f,
            accentColor,
            true
        )

        assistantCard.addView(assistantLabel)

        val microphone = TextView(this).apply {
            text = "🎙️"
            textSize = 43f
            gravity = Gravity.CENTER
            background = rounded(accentColor, 100)
        }

        assistantCard.addView(
            microphone,
            LinearLayout.LayoutParams(dp(112), dp(112)).apply {
                topMargin = dp(25)
                bottomMargin = dp(20)
            }
        )

        statusText = makeText(
            "READY TO LISTEN",
            16f,
            whiteColor,
            true
        )

        assistantCard.addView(statusText)

        val description = makeText(
            "Speak naturally. AKHIL is ready for your commands.",
            13f,
            mutedColor,
            false
        )

        description.gravity = Gravity.CENTER
        assistantCard.addView(description, marginParams(top = 10))

        layout.addView(
            assistantCard,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // Start button
        val startButton = makeButton(
            "START LISTENING",
            accentColor,
            Color.rgb(5, 25, 30)
        )

        startButton.setOnClickListener {
            requestMicrophonePermission()
        }

        layout.addView(startButton, marginParams(top = 25))

        // Stop button
        val stopButton = makeButton(
            "STOP LISTENING",
            cardColor,
            whiteColor
        )

        stopButton.background = rounded(
            cardColor,
            16,
            mutedColor
        )

        stopButton.setOnClickListener {
            stopService(Intent(this, VoiceService::class.java))
            statusText.text = "ASSISTANT STOPPED"
        }

        layout.addView(stopButton, marginParams(top = 12))

        // Commands section
        val commandsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = rounded(cardColor, 20)
        }

        commandsCard.addView(
            makeText("TRY THESE COMMANDS", 14f, accentColor, true)
        )

        val commands = listOf(
            "Open WhatsApp",
            "Open Chrome",
            "Open Facebook",
            "Open ChatGPT",
            "Open Settings"
        )

        commands.forEach {
            commandsCard.addView(
                makeText("•  $it", 14f, whiteColor, false),
                marginParams(top = 12)
            )
        }

        layout.addView(commandsCard, marginParams(top = 25))

        val footer = makeText(
            "AKHIL ASSISTANT • YOUR VOICE, YOUR CONTROL",
            10f,
            mutedColor,
            false
        )

        footer.gravity = Gravity.CENTER
        layout.addView(footer, marginParams(top = 28))

        scrollView.addView(layout)
        setContentView(scrollView)

        MorningAlarmReceiver.schedule(this)

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST
            )
        }
    }

    private fun requestMicrophonePermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MICROPHONE_PERMISSION_REQUEST
            )
        } else {
            startVoiceService()
        }
    }

    private fun startVoiceService() {
        val serviceIntent = Intent(this, VoiceService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        statusText.text = "VOICE ASSISTANT IS RUNNING"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == MICROPHONE_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startVoiceService()
            } else {
                statusText.text = "MICROPHONE PERMISSION NEEDED"
            }
        }
    }

    private fun makeText(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            typeface = if (bold) {
                Typeface.DEFAULT_BOLD
            } else {
                Typeface.DEFAULT
            }
        }
    }

    private fun makeButton(
        label: String,
        color: Int,
        textColor: Int
    ): Button {
        return Button(this).apply {
            text = label
            textSize = 14f
            isAllCaps = false
            setTextColor(textColor)
            typeface = Typeface.DEFAULT_BOLD
            background = rounded(color, 16)
            minHeight = dp(56)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
    }

    private fun rounded(
        color: Int,
        radius: Int,
        strokeColor: Int? = null
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (strokeColor != null) {
                setStroke(dp(1), strokeColor)
            }
        }
    }

    private fun marginParams(
        top: Int = 0,
        bottom: Int = 0
    ): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(top)
            bottomMargin = dp(bottom)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
