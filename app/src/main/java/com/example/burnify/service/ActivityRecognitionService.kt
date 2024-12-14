package com.example.burnify.service

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionService : IntentService("ActivityRecognitionService") {

    override fun onHandleIntent(intent: Intent?) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = intent?.let { ActivityRecognitionResult.extractResult(it) }
            val detectedActivities = result?.probableActivities

            if (detectedActivities != null) {
                for (activity in detectedActivities) {
                    val activityName = getActivityName(activity.type)
                    val confidence = activity.confidence
                    Log.d("ActivityRecognition", "Activity: $activityName, Confidence: $confidence")
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
