package com.example.burnify.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.burnify.R
import com.example.burnify.receiver.ActivityRecognitionReceiver
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.tasks.Task

class ActivityRecognitionService : Service() {

    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var pendingIntent: PendingIntent
    private val DETECTION_INTERVAL_IN_MILLISECONDS: Long = 3000

    companion object {
        const val CHANNEL_ID = "activity_recognition_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        activityRecognitionClient = ActivityRecognition.getClient(this)
        val intent = Intent(this, ActivityRecognitionReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        createNotificationChannel()
        startForegroundService()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundService() {
        val notificationIntent = Intent(this, com.example.burnify.activity.MainActivity::class.java)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Activity Recognition Service")
            .setContentText("Detecting activities...")
            .setSmallIcon(R.mipmap.ic_launcher) // Use your app's launcher icon instead
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // We are handling the permission check right here
        try {
            requestActivityUpdates()
        } catch (e: SecurityException) {
            Log.e("ActivityRecognitionService", "SecurityException: ${e.message}")
            // Handle the exception accordingly
        }
        return START_STICKY
    }

    private fun requestActivityUpdates() {
        // Check if the permission is granted before proceeding
        if (hasRequiredPermissions()) {
            try {
                val task: Task<Void> = activityRecognitionClient.requestActivityUpdates(DETECTION_INTERVAL_IN_MILLISECONDS, pendingIntent)
                task.addOnSuccessListener {
                    Log.d("ActivityRecognitionService", "Activity updates started successfully.")
                }
                task.addOnFailureListener { e ->
                    Log.e("ActivityRecognitionService", "Failed to request activity updates.", e)
                }
            } catch (e: SecurityException) {
                Log.e("ActivityRecognitionService", "SecurityException while requesting activity updates: ${e.message}")
                // Handle the exception accordingly
                val broadcastIntent = Intent("permission_denied")
                sendBroadcast(broadcastIntent)
            }
        } else {
            Log.e("ActivityRecognitionService", "Permission not granted")
            // Notify the main activity that permission is denied
            val broadcastIntent = Intent("permission_denied")
            sendBroadcast(broadcastIntent)
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        val activityRecognitionPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return activityRecognitionPermission == PackageManager.PERMISSION_GRANTED &&
                fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        removeActivityUpdates()
        super.onDestroy()
    }

    @SuppressLint("MissingPermission")
    private fun removeActivityUpdates() {
        val task = activityRecognitionClient.removeActivityUpdates(pendingIntent)
        task.addOnSuccessListener {
            Log.d("ActivityRecognitionService", "Activity updates removed")
        }
        task.addOnFailureListener { e ->
            Log.e("ActivityRecognitionService", "Failed to remove activity updates", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}