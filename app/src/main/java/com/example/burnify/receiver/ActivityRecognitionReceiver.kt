package com.example.burnify.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
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

                    // Log the detected activity and confidence
                    Log.d("ActivityRecognition", "Activity: $activityName, Confidence: $confidence")

                    // Send the activity data to MainActivity through LocalBroadcast
                    val broadcastIntent = Intent("com.example.burnify.UPDATE_UI")
                    broadcastIntent.putExtra("activity", activityName)
                    broadcastIntent.putExtra("confidence", confidence)
                    context.sendBroadcast(broadcastIntent)
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
