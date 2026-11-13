
package com.akhil.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private val permissionRequestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }

        val title = TextView(this).apply {
            text = "AKHIL Assistant"
            textSize = 26f
        }

        statusText = TextView(this).apply {
            text = "Ready to start"
            textSize = 18f
        }

        val startButton = Button(this).apply {
            text = "START LISTENING"
            setOnClickListener {
                requestNeededPermissions()
            }
        }

        val stopButton = Button(this).apply {
            text = "STOP LISTENING"
            setOnClickListener {
                stopService(Intent(this@MainActivity, VoiceService::class.java))
                statusText.text = "Voice service stopped"
            }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(startButton)
        layout.addView(stopButton)

        setContentView(layout)

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                permissionRequestCode
            )
        }

        MorningAlarmReceiver.schedule(this)
    }

    private fun requestNeededPermissions() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                permissionRequestCode
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

        statusText.text = "Voice service is running"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionRequestCode &&
            permissions.contains(Manifest.permission.RECORD_AUDIO) &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startVoiceService()
        } else if (requestCode == permissionRequestCode) {
            statusText.text = "Microphone permission is needed"
        }
    }
}
