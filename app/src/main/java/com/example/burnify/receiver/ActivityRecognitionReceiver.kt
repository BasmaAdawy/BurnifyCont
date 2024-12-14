package com.example.burnify.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            val detectedActivities = result?.probableActivities

            if (detectedActivities != null) {
                for (activity in detectedActivities) {
                    val activityName = getActivityName(activity.type)
                    val confidence = activity.confidence

                    Log.d("ActivityRecognitionReceiver", "Activity: $activityName, Confidence: $confidence")

                    // Send broadcast with activity update
                    val broadcastIntent = Intent("activity_recognition_update")
                    broadcastIntent.putExtra("activity", activityName)
                    broadcastIntent.putExtra("confidence", confidence)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
                }
            }
        }
    }

    private fun getActivityName(type: Int): String {
        return when (type) {
            DetectedActivity.IN_VEHICLE -> "In Vehicle"
            DetectedActivity.ON_BICYCLE -> "On Bicycle"
            DetectedActivity.ON_FOOT -> "On Foot"
            DetectedActivity.RUNNING -> "Running"
            DetectedActivity.STILL -> "Still"
            DetectedActivity.WALKING -> "Walking"
            DetectedActivity.TILTING -> "Tilting"
            DetectedActivity.UNKNOWN -> "Unknown"
            else -> "Unidentified"
        }
    }
}