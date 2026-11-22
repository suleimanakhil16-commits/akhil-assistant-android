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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    companion object {
        private const val MICROPHONE_PERMISSION_REQUEST = 100
        private const val NOTIFICATION_PERMISSION_REQUEST = 101
    }

    private val bg = Color.rgb(4, 8, 8)
    private val panel = Color.rgb(13, 25, 24)
    private val green = Color.rgb(0, 232, 112)
    private val white = Color.WHITE
    private val muted = Color.rgb(190, 211, 201)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this).apply { setBackgroundColor(bg); isFillViewport = true }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(22))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val mic = TextView(this).apply {
            text = "🎙"; textSize = 30f; gravity = Gravity.CENTER
            setTextColor(green); background = circle(panel, green)
        }
        header.addView(mic, LinearLayout.LayoutParams(dp(58), dp(58)))
        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        brand.addView(text("AKHIL VOICE CONTROL AI", 19f, white, true))
        brand.addView(text("Your Smart Voice Assistant 💚", 13f, green, false), params(top = 3))
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(text("⚙", 27f, white, true))
        root.addView(header)

        val portrait = ImageView(this).apply {
            setImageResource(R.drawable.akhil_female_avatar)
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = "AKHIL female assistant portrait"
            background = rounded(panel, 24, green)
            clipToOutline = true
        }
        root.addView(portrait, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(330)
        ).apply { topMargin = dp(18) })

        val greeting = TextView(this).apply {
            text = "Hello Suleiman 👋\nHow can I help you today?"
            textSize = 18f; setTextColor(white); gravity = Gravity.CENTER
            setPadding(dp(16), dp(15), dp(16), dp(15))
            background = rounded(panel, 22, Color.rgb(30, 67, 51))
        }
        root.addView(greeting, params(top = 14))

        val listen = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER
            setPadding(dp(18), dp(12), dp(18), dp(12))
            background = rounded(panel, 30, green)
        }
        listen.addView(text("▮  ▮  ▮", 19f, green, true))
        statusText = text("  READY TO LISTEN", 16f, white, true)
        listen.addView(statusText)
        root.addView(listen, LinearLayout.LayoutParams(-2, -2).apply {
            gravity = Gravity.CENTER; topMargin = dp(16)
        })

        val start = button("🎙  START LISTENING", green, Color.BLACK)
        start.setOnClickListener { requestMicrophonePermission() }
        root.addView(start, params(top = 14))
        val stop = button("STOP LISTENING", panel, white)
        stop.setOnClickListener {
            stopService(Intent(this, VoiceService::class.java))
            statusText.text = "  LISTENING STOPPED"
        }
        root.addView(stop, params(top = 8))

        val shortcuts = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val items = listOf("WhatsApp" to "☎", "Chrome" to "🌐", "Facebook" to "f", "ChatGPT" to "✳", "Setting" to "⚙")
        items.forEach { (name, symbol) ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
                setPadding(dp(3), dp(10), dp(3), dp(5))
            }
            val icon = TextView(this).apply {
                text = symbol; textSize = 23f; setTextColor(white); gravity = Gravity.CENTER
                background = circle(panel, green)
            }
            item.addView(icon, LinearLayout.LayoutParams(dp(52), dp(52)))
            val label = text(name, 10f, white, true).apply { gravity = Gravity.CENTER }
            item.addView(label, params(top = 5))
            shortcuts.addView(item, LinearLayout.LayoutParams(0, -2, 1f))
        }
        root.addView(shortcuts, params(top = 14))
        scroll.addView(root)
        setContentView(scroll)

        MorningAlarmReceiver.schedule(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST)
    }

    private fun requestMicrophonePermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), MICROPHONE_PERMISSION_REQUEST)
        } else startVoiceService()
    }

    private fun startVoiceService() {
        val intent = Intent(this, VoiceService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
        statusText.text = "  LISTENING..."
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MICROPHONE_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) startVoiceService()
            else statusText.text = "  MICROPHONE PERMISSION NEEDED"
        }
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply {
        text = value; textSize = size; setTextColor(color)
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }

    private fun button(label: String, color: Int, textColor: Int) = Button(this).apply {
        text = label; textSize = 14f; isAllCaps = false; setTextColor(textColor)
        typeface = Typeface.DEFAULT_BOLD; background = rounded(color, 16)
        minHeight = dp(54); setPadding(dp(12), dp(8), dp(12), dp(8))
    }

    private fun rounded(color: Int, radius: Int, stroke: Int? = null) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
        if (stroke != null) setStroke(dp(2), stroke)
    }

    private fun circle(color: Int, stroke: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL; setColor(color); setStroke(dp(2), stroke)
    }

    private fun params(top: Int = 0) = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(top) }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}