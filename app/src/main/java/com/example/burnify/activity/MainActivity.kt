package com.example.burnify.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.burnify.R
import com.example.burnify.service.ActivityRecognitionService

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_PERMISSIONS = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startServiceButton: Button = findViewById(R.id.start_service_button)
        startServiceButton.setOnClickListener {
            if (arePermissionsGranted()) {
                startActivityRecognitionService()
            } else {
                requestPermissions()
            }
        }
    }

    private fun arePermissionsGranted(): Boolean {
        val activityRecognitionPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        return activityRecognitionPermission == PackageManager.PERMISSION_GRANTED && fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_CODE_PERMISSIONS)
    }

    private fun startActivityRecognitionService() {
        val intent = Intent(this, ActivityRecognitionService::class.java)
        startService(intent)
        Toast.makeText(this, "Activity Recognition Service Started", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startActivityRecognitionService()
            } else {
                Toast.makeText(this, "Please grant permissions to start the service", Toast.LENGTH_SHORT).show()
            }
        }
    }
}