package com.example.burnify.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult

class ActivityRecognitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ActivityRecognitionReceiver", "Broadcast received")
        val result = ActivityRecognitionResult.extractResult(intent)

        result?.let {
            val mostProbableActivity = it.mostProbableActivity
            val activityType = mostProbableActivity.type
            val confidence = mostProbableActivity.confidence

            Log.d("ActivityRecognitionReceiver", "Detected activity: $activityType with confidence: $confidence")

            // Send the detected activity
            val activityIntent = Intent("activity_update")
            activityIntent.putExtra("activity_type", activityType)
            activityIntent.putExtra("confidence", confidence)
            context.sendBroadcast(activityIntent)
        } ?: run {
            Log.e("ActivityRecognitionReceiver", "No activity recognition result found.")
        }
    }
}