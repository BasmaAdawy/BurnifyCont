package com.example.burnify.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.burnify.R
import com.example.burnify.service.ActivityRecognitionService
import com.google.android.gms.location.DetectedActivity

class MainActivity : AppCompatActivity() {

    private lateinit var activityTextView: TextView
    private lateinit var caloriesTextView: TextView

    private val activityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val activityType = intent.getIntExtra("activity_type", -1)
            val confidence = intent.getIntExtra("confidence", 0)
            val caloriesBurned = calculateCalories(activityType, confidence)

            // Update UI on the main thread
            runOnUiThread {
                activityTextView.text = "Detected Activity: ${getActivityString(activityType)}"
                caloriesTextView.text = "Calories Burned: $caloriesBurned"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityTextView = findViewById(R.id.activity_text_view)
        caloriesTextView = findViewById(R.id.calories_text_view)

        // Check and request permissions
        checkPermissions()

        // Register receiver for activity updates
        LocalBroadcastManager.getInstance(this).registerReceiver(activityReceiver, IntentFilter("activity_update"))

        val startServiceButton: Button = findViewById(R.id.start_service_button)
        startServiceButton.setOnClickListener {
            startActivityRecognitionService()
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION),
                1000)
        } else {
            // Permissions already granted
            startActivityRecognitionService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted
                startActivityRecognitionService()
            } else {
                // Permissions denied
                Log.e("MainActivity", "Permissions denied")
            }
        }
    }

    private fun calculateCalories(activityType: Int, confidence: Int): Int {
        val baseCalories = when (activityType) {
            DetectedActivity.WALKING -> 4
            DetectedActivity.RUNNING -> 8
            DetectedActivity.ON_BICYCLE -> 6
            DetectedActivity.STILL -> 1
            DetectedActivity.IN_VEHICLE -> 2
            DetectedActivity.TILTING -> 1
            DetectedActivity.ON_FOOT -> 5
            else -> 0
        }
        return (baseCalories * confidence / 100)
    }

    private fun getActivityString(activityType: Int): String {
        return when (activityType) {
            DetectedActivity.WALKING -> "Walking"
            DetectedActivity.RUNNING -> "Running"
            DetectedActivity.ON_BICYCLE -> "Cycling"
            DetectedActivity.STILL -> "Still"
            DetectedActivity.IN_VEHICLE -> "In Vehicle"
            DetectedActivity.TILTING -> "Tilting"
            DetectedActivity.ON_FOOT -> "On Foot"
            else -> "Unknown Activity"
        }
    }

    private fun startActivityRecognitionService() {
        val intent = Intent(this, ActivityRecognitionService::class.java)
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(activityReceiver)
    }
}